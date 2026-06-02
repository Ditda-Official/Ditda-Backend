package ditda.backend.domain.commission.core.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommissionErrorCode implements BaseErrorCode {

	// 새 외주 작성 파일 업로드
	COMMISSION_FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMISSION_400_01", "첨부 파일 개수가 허용 범위를 초과했습니다."),
	INVALID_COMMISSION_FILE(HttpStatus.BAD_REQUEST, "COMMISSION_400_02", "지원하지 않는 파일 형식입니다."),
	COMMISSION_FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "COMMISSION_413_01", "첨부 파일 크기가 제한을 초과했습니다."),
	PORTFOLIO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMISSION_500_01", "파일 업로드에 실패했습니다."),

	COLORS_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_03", "직접 색상 선택 시 색상은 필수입니다."),
	SETTLEMENT_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "COMMISSION_400_04", "결제 약관 동의는 필수입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
