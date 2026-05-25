package ditda.backend.domain.common.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(

	@Schema(description = "로그인 유저 ID", example = "1")
	Long userId,

	@Schema(description = "Access Token")
	String accessToken
) {
}
