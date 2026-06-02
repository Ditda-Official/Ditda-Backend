package ditda.backend.domain.commission.core.dto.request;

import ditda.backend.domain.commission.core.entity.enums.FileKind;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "외주 첨부파일 presigned URL 발급 요청")
public record CommissionFilePresignRequest(

	@Schema(description = "파일 종류", example = "MATERIAL")
	@NotNull(message = "파일 종류는 필수입니다.")
	FileKind fileKind,

	@Schema(description = "파일 Content-Type", example = "image/png")
	@NotBlank(message = "Content-Type은 필수입니다.")
	String contentType
) {
}
