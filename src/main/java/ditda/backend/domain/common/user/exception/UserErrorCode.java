package ditda.backend.domain.common.user.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

	USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_01", "이미 사용 중인 아이디입니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_02", "이미 사용 중인 이메일입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_01", "존재하지 않는 유저입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
