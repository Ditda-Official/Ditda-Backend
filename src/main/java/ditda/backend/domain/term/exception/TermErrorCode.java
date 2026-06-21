package ditda.backend.domain.term.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TermErrorCode implements BaseErrorCode {

	REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERM_400_01", "필수 약관에 동의해야 합니다."),
	SETTLEMENT_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERM_400_02", "결제 약관 동의는 필수입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
