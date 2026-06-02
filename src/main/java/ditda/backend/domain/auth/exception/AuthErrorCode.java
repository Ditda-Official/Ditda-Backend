package ditda.backend.domain.auth.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

	EMAIL_VERIFICATION_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_01", "잠시 후 다시 시도해주세요."),
	EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_400_01", "인증번호가 만료되었거나 발급되지 않았습니다."),
	EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "AUTH_400_02", "인증번호가 일치하지 않습니다."),
	EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_400_03", "이메일 인증이 완료되지 않았습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
