package ditda.backend.global.encryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AesEncryptor {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int IV_LENGTH = 12;          // IV 길이
	private static final int GCM_TAG_LENGTH = 128;    // 태그 길이

	private static final SecureRandom RANDOM = new SecureRandom();

	@Value("${encrypt.secret-key}")
	private String secretKey;

	private SecretKeySpec secretKeySpec;

	@PostConstruct
	public void init() {
		try {
			byte[] keyBytes = MessageDigest.getInstance("SHA-256")
				.digest(secretKey.getBytes(StandardCharsets.UTF_8));
			this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");

		} catch (Exception e) {
			throw new IllegalStateException("Failed to initialize AES encryption", e);
		}
	}

	public String encrypt(String plainText) {
		if (plainText == null || plainText.isEmpty()) {
			return null;
		}

		try {
			// 무작위 IV 생성
			byte[] iv = new byte[IV_LENGTH];
			RANDOM.nextBytes(iv);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

			// Cipher 초기화 및 암호화
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			// IV + 암호문 결합
			byte[] combined = new byte[IV_LENGTH + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
			System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

			// Base64 인코딩
			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			log.error("Failed to encrypt data", e);
			throw new GeneralException(GeneralErrorCode.ENCRYPTION_FAILED);
		}
	}

	public String decrypt(String encryptedText) {
		if (encryptedText == null || encryptedText.isEmpty()) {
			return null;
		}

		try {
			// Base64 디코딩
			byte[] combined = Base64.getDecoder().decode(encryptedText);

			// IV 추출
			byte[] iv = new byte[IV_LENGTH];
			System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

			// 암호문 추출
			byte[] encrypted = new byte[combined.length - IV_LENGTH];
			System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

			// Cipher 초기화 및 복호화
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);

			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Failed to decrypt data", e);
			throw new GeneralException(GeneralErrorCode.DECRYPTION_FAILED);
		}
	}
}
