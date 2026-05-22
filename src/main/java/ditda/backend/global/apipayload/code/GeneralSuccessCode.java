package ditda.backend.global.apipayload.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {

	OK(HttpStatus.OK, "SUCCESS", "성공적으로 처리됐습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
