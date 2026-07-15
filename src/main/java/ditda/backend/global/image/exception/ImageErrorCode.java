package ditda.backend.global.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageErrorCode {

	IMAGE_NOT_READABLE("이미지 파일을 읽을 수 없습니다."),
	IMAGE_RESOLUTION_EXCEEDED("이미지 해상도가 제한을 초과했습니다.");

	private final String message;
}
