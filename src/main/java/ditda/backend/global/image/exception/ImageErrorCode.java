package ditda.backend.global.image.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {

	IMAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "IMAGE_400_01", "이미지 파일을 읽을 수 없습니다."),
	IMAGE_RESOLUTION_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "IMAGE_413_01", "이미지 해상도가 제한을 초과했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
