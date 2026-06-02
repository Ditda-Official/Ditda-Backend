package ditda.backend.domain.designer.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "포트폴리오 presigned URL 발급 요청")
public record PortfolioPresignRequest(

	@Schema(description = "인증 완료된 이메일", example = "testid@gmail.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@Schema(description = "파일 Content-Type", example = "application/pdf")
	@NotBlank(message = "Content-Type은 필수입니다.")
	String contentType
) {
}
