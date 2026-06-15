package ditda.backend.domain.commission.draft.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DraftErrorCode implements BaseErrorCode {

	DRAFTS_NOT_READY(HttpStatus.CONFLICT, "DRAFT_409_01", "아직 모든 디자이너가 시안을 제출하지 않았습니다."),
	DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAFT_404_01", "시안을 찾을 수 없습니다."),
	DRAFT_INVALID_ROUND(HttpStatus.CONFLICT, "DRAFT_409_02", "해당 시안은 1차 시안이 아닙니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
