package ditda.backend.global.s3.dto.response;

import ditda.backend.global.s3.PresignedUpload;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 presigned URL 발급 응답")
public record PresignResponse(

	@Schema(description = "S3 객체 key", example = "commission/tmp/{uuid}.png")
	String key,

	@Schema(description = "S3 직접 업로드용 presigned PUT URL")
	String presignedUrl
) {

	public static PresignResponse from(PresignedUpload upload) {
		return new PresignResponse(upload.key(), upload.presignedUrl());
	}
}
