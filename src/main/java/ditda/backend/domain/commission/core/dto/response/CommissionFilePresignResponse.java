package ditda.backend.domain.commission.core.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "외주 첨부파일 presigned URL 발급 응답")
public record CommissionFilePresignResponse(

	@Schema(description = "S3 객체 key", example = "commission/tmp/{uuid}.png")
	String key,

	@Schema(description = "S3 직접 업로드용 presigned PUT URL")
	String presignedUrl
) {
}
