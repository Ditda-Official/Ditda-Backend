package ditda.backend.global.s3.dto.request;

import ditda.backend.global.s3.enums.UploadTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "파일 업로드 presigned URL 발급 요청")
public record PresignRequest(

	@Schema(description = "업로드 대상", example = "COMMISSION_MATERIAL")
	@NotNull(message = "업로드 대상은 필수입니다.")
	UploadTarget target,

	@Schema(description = "파일 Content-Type", example = "image/png")
	@NotBlank(message = "Content-Type은 필수입니다.")
	String contentType
) {
}
