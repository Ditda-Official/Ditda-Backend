package ditda.backend.domain.common.term.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TermErrorCode implements BaseErrorCode {

	REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERM_400_01", "필수 약관에 동의해야 합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
