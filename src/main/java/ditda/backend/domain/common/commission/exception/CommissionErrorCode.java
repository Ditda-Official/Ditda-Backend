package ditda.backend.domain.common.commission.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommissionErrorCode implements BaseErrorCode {

	// === 외주 생성 검증 ===
	FIRST_DRAFT_DEADLINE_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_01", "1차 시안 마감일은 필수입니다."),
	FIRST_DRAFT_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_02", "1차 시안 마감일은 오늘로부터 최소 10일 뒤여야 합니다."),
	FINAL_DEADLINE_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_03", "최종 마감일은 필수입니다."),
	FINAL_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "COMMISSION_400_04", "최종 마감일은 1차 시안 마감일로부터 최소 14일 뒤여야 합니다."),

	// === 수정 요청 검증 ===
	REVISION_COMMENT_REQUIRED(HttpStatus.BAD_REQUEST, "COMMISSION_400_05", "수정 요청 코멘트는 비어있을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
