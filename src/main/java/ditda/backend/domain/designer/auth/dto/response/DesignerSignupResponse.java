package ditda.backend.domain.designer.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 회원가입 응답")
public record DesignerSignupResponse(

	@Schema(description = "User ID")
	Long userId,

	@Schema(description = "Access Token")
	String accessToken
) {
}
