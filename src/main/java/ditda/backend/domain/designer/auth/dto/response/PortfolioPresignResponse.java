package ditda.backend.domain.designer.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PortfolioPresignResponse(

	@Schema(description = "S3 객체 key", example = "portfolio/5a5237a7-674a-47c2-91ca-6fdb28ba6b2d.pdf")
	String key,

	@Schema(description = "S3 직접 업드드용 presigned PUT URL", example = "\"https://{bucket}.s3.{region}.amazonaws.com/portfolio/{uuid}.pdf?X-Amz-Signature=...")
	String presignedUrl
) {
}
