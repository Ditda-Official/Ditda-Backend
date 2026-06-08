package ditda.backend.global.s3.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3ContentType {

	PNG("image/png", ".png"),
	JPEG("image/jpeg", ".jpeg"),
	PDF("application/pdf", ".pdf");

	private final String contentType;
	private final String extension;

	public static S3ContentType from(String contentType) {
		return Arrays.stream(values())
			.filter(t -> t.contentType.equals(contentType))
			.findFirst()
			.orElse(null);
	}
}
