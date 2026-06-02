package ditda.backend.domain.auth.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioErrorCode implements BaseErrorCode {

	PORTFOLIO_FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "DESIGNER_400_01", "포트폴리오 파일은 최대 3개까지 업로드 가능합니다."),
	INVALID_PORTFOLIO_FILE(HttpStatus.BAD_REQUEST, "DESIGNER_400_02", "지원하지 않는 파일 형식입니다."),
	PORTFOLIO_FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "DESIGNER_413_01", "포트폴리오 파일 크기가 제한을 초과했습니다."),
	PORTFOLIO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DESIGNER_500_01", "포트폴리오 파일 업로드에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
