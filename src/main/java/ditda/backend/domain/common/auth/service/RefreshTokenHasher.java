package ditda.backend.domain.common.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

	private final SecretKeySpec secretKeySpec;

	public RefreshTokenHasher(
		@Value("${jwt.refresh-token-hash-secret}") String secret
	) {
		this.secretKeySpec = new SecretKeySpec(
			secret.getBytes(StandardCharsets.UTF_8),
			"HmacSHA256"
		);
	}

	public String hash(String refreshToken) {

		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKeySpec);

			byte[] digest = mac.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));

			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(digest);
		} catch (NoSuchAlgorithmException | InvalidKeyException exception) {
			throw new IllegalStateException("Refresh token hash 생성 실패", exception);
		}
	}
}
