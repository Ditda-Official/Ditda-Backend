package ditda.backend.domain.auth.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import ditda.backend.domain.auth.exception.AuthErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final String CODE_KEY_FORMAT = "auth:email:code:%s";
	private static final String VERIFIED_KEY_FORMAT = "auth:email:verified:%s";
	private static final String RESEND_LOCK_KEY_FORMAT = "auth:email:resend-lock:%s";

	private static final Duration CODE_TTL = Duration.ofMinutes(5);             // 인증 코드 유효시간 5분
	private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);        // 인증 완료 유효시간 30분
	private static final Duration RESEND_LOCK_TTL = Duration.ofSeconds(60);     // 코드 재발송 제한시간 1분

	private static final int CODE_BOUND = 10_000;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final StringRedisTemplate redisTemplate;

	public String issueCode(String email) {
		String lockKey = RESEND_LOCK_KEY_FORMAT.formatted(email);

		if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
			throw new GeneralException(AuthErrorCode.EMAIL_VERIFICATION_COOLDOWN);
		}

		String code = generateCode();
		redisTemplate.opsForValue().set(codeKey(email), code, CODE_TTL);
		redisTemplate.opsForValue().set(lockKey, "1", RESEND_LOCK_TTL);

		log.info("Email verification code issued. email={}", email);

		return code;
	}

	public void verifyCode(String email, String code) {
		String savedCode = redisTemplate.opsForValue().get(codeKey(email));

		if (savedCode == null) {
			throw new GeneralException(AuthErrorCode.EMAIL_CODE_EXPIRED);
		}
		if (!savedCode.equals(code)) {
			throw new GeneralException(AuthErrorCode.EMAIL_CODE_INVALID);
		}

		redisTemplate.delete(codeKey(email));
		redisTemplate.opsForValue().set(verifiedKey(email), "true", VERIFIED_TTL);
	}

	public void validateVerified(String email) {
		String verified = redisTemplate.opsForValue().get(verifiedKey(email));
		if (!"true".equals(verified)) {
			throw new GeneralException(AuthErrorCode.EMAIL_NOT_VERIFIED);
		}
	}

	public void deleteVerified(String email) {
		redisTemplate.delete(verifiedKey(email));
	}

	private String generateCode() {
		return String.format("%04d", RANDOM.nextInt(CODE_BOUND));
	}

	private String codeKey(String email) {
		return CODE_KEY_FORMAT.formatted(email);
	}

	private String verifiedKey(String email) {
		return VERIFIED_KEY_FORMAT.formatted(email);
	}
}

