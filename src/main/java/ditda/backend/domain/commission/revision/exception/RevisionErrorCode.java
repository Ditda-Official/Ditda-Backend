package ditda.backend.domain.commission.revision.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RevisionErrorCode implements BaseErrorCode {

	// 수정 요청 생성 에러
	DUPLICATE_REVISION_CATEGORY(HttpStatus.BAD_REQUEST, "REVISION_400_01", "동일한 카테고리는 중복될 수 없습니다."),
	REVISION_ALREADY_REQUESTED(HttpStatus.CONFLICT, "REVISION_409_01", "이미 해당 시안에 대한 수정 요청이 존재합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
