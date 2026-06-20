package ditda.backend.domain.commission.revision.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RevisionErrorCode implements BaseErrorCode {

	COMMISSION_NOT_REVISABLE(HttpStatus.CONFLICT, "REVISION_409_01", "수정 단계의 외주가 아닙니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
