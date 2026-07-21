package ditda.backend.global.image.exception;

import lombok.Getter;

@Getter
public class ImageProcessingException extends RuntimeException {

	private final ImageErrorCode errorCode;

	public ImageProcessingException(ImageErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
