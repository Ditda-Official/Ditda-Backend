package ditda.backend.domain.commission.application.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationErrorCode implements BaseErrorCode {

	INVALID_STATUS_FOR_DRAFT_SELECTION(HttpStatus.BAD_REQUEST, "APP_400_01", "1차 시안 제출 완료 상태에서만 결과를 선택할 수 있습니다."),
	INVALID_STATUS_FOR_DRAFT_REJECTION(HttpStatus.BAD_REQUEST, "APP_400_02", "1차 시안 제출 완료 상태에서만 탈락 처리할 수 있습니다."),
	INVALID_STATUS_FOR_ASSIGNMENT(HttpStatus.BAD_REQUEST, "APP_400_03", "지원 완료 상태에서만 1차 시안 대상자로 지정할 수 있습니다."),
	INVALID_STATUS_FOR_DRAFT_MISSED(HttpStatus.BAD_REQUEST, "APP_400_04", "1차 시안 대상자 상태에서만 미제출 처리할 수 있습니다."),
	INVALID_STATUS_FOR_APP_REJECTED(HttpStatus.BAD_REQUEST, "APP_400_05", "지원 완료 상태에서만 지원 탈락 처리할 수 있습니다."),
	INVALID_STATUS_FOR_DRAFT_SUBMITTED(HttpStatus.BAD_REQUEST, "APP_400_06", "1차 시안 대상자 상태에서만 시안을 제출할 수 있습니다."),
	INVALID_STATUS_FOR_CANCEL(HttpStatus.BAD_REQUEST, "APP_400_07", "지원 완료 상태에서만 취소할 수 있습니다."),
	APPLICATION_ALREADY_DRAFT_SUBMITTED(HttpStatus.CONFLICT, "APP_409_01", "이미 1차 시안을 제출했습니다."),
	NOT_SELECTED_DESIGNER(HttpStatus.BAD_REQUEST, "APP_400_08", "최종 선택된 디자이너가 아닙니다."),
	APPLICATION_ALREADY_APPLIED(HttpStatus.CONFLICT, "APP_409_02", "이미 지원한 외주입니다."),
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APP_404_01", "해당 외주에 지원한 이력이 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
