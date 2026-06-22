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
	COMMISSION_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMISSION_500_01", "파일 업로드에 실패했습니다."),

	// 색상 선택 (직접 색상 지정)
	COLORS_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_02", "직접 색상 선택 시 색상은 필수입니다."),
	INVALID_COLOR_COMPOSITION(
		HttpStatus.BAD_REQUEST,
		"COMMISSION_400_03",
		"색상 구성이 올바르지 않습니다. MAIN, SUB1, SUB2를 각 1개씩 지정해야 합니다."
	),

	// 카테고리별
	UNSUPPORTED_CATEGORY(HttpStatus.BAD_REQUEST, "COMMISSION_400_04", "지원하지 않는 카테고리입니다."),
	TEXTBOOK_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_05", "교재 정보는 필수입니다."),
	INVALID_PAGE_TYPE_FOR_CATEGORY(HttpStatus.BAD_REQUEST, "COMMISSION_400_06", "해당 카테고리에서 지원하지 않는 페이지 종류입니다."),

	// 마감 기한
	INVALID_DEADLINE_ORDER(HttpStatus.BAD_REQUEST, "COMMISSION_400_07", "1차 시안 마감일은 최종 마감일보다 빨라야 합니다."),
	FIRST_DRAFT_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_08", "1차 시안 마감일은 오늘로부터 최소 10일 이후여야 합니다."),
	FINAL_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_09", "최종 마감일은 1차 시안 마감일로부터 최소 14일 이후여야 합니다."),

	// 외주 조회 및 선택
	COMMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMISSION_404_01", "외주를 찾을 수 없습니다."),
	COMMISSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMISSION_403_01", "해당 외주에 대한 권한이 없습니다."),
	COMMISSION_STATUS_INVALID(HttpStatus.CONFLICT, "COMMISSION_409_01", "외주 상태가 올바르지 않습니다."),
	DESIGNER_ALREADY_SELECTED(HttpStatus.CONFLICT, "COMMISSION_409_02", "이미 디자이너가 확정된 외주입니다."),

	// 외주 상태
	COMMISSION_NOT_REVISABLE(HttpStatus.CONFLICT, "COMMISSION_409_03", "수정 단계의 외주가 아닙니다."),
	COMMISSION_NOT_FINALIZABLE(HttpStatus.CONFLICT, "COMMISSION_409_04", "최종 확정 가능한 외주 상태가 아닙니다."),

	// 외주 수정 제한
	REVISION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "COMMISSION_409_05", "수정 횟수를 모두 사용했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
