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
	INVALID_STATUS_FOR_DRAFT_MISSED(HttpStatus.BAD_REQUEST, "APP_400_04", "1차 시안 대상자 상태에서만 미제출 처리할 수 있습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
