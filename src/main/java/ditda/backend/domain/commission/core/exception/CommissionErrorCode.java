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
	COMMISSION_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMISSION_500_01", "파일 업로드에 실패했습니다."),

	// 색상 선택 (직접 색상 지정)
	COLORS_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_03", "직접 색상 선택 시 색상은 필수입니다."),
	INVALID_COLOR_COMPOSITION(
		HttpStatus.BAD_REQUEST,
		"COMMISSION_400_04",
		"색상 구성이 올바르지 않습니다. MAIN, SUB1, SUB2를 각 1개씩 지정해야 합니다."
	),

	// 카테고리별
	UNSUPPORTED_CATEGORY(HttpStatus.BAD_REQUEST, "COMMISSION_400_05", "지원하지 않는 카테고리입니다."),
	TEXTBOOK_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_06", "교재 정보는 필수입니다."),
	INVALID_PAGE_TYPE_FOR_CATEGORY(HttpStatus.BAD_REQUEST, "COMMISSION_400_07", "해당 카테고리에서 지원하지 않는 페이지 종류입니다."),

	// 마감 기한
	INVALID_DEADLINE_ORDER(HttpStatus.BAD_REQUEST, "COMMISSION_400_08", "1차 시안 마감일은 최종 마감일보다 빨라야 합니다."),
	FIRST_DRAFT_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_09", "1차 시안 마감일은 오늘로부터 최소 10일 이후여야 합니다."),
	FINAL_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_10", "최종 마감일은 1차 시안 마감일로부터 최소 14일 이후여야 합니다."),

	// 외주
	COMMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMISSION_404_01", "외주를 찾을 수 없습니다."),
	COMMISSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMISSION_403_01", "해당 외주에 대한 권한이 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
