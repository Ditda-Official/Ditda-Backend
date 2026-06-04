package ditda.backend.domain.instructor.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InstructorErrorCode implements BaseErrorCode {

	INSTRUCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "INSTRUCTOR_404_01", "강사를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
