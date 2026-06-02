package ditda.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record ReissueResponse(

	@Schema(description = "Access Token")
	String accessToken
) {
}
