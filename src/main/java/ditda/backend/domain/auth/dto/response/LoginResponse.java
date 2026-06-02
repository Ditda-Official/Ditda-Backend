package ditda.backend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(

	@Schema(description = "로그인 유저 ID", example = "1")
	Long userId,

	@Schema(description = "사용자 이름", example = "홍길동")
	String name,

	@Schema(description = "프로필 이미지", example = "https://example.com/profile/default.png")
	String profileImageUrl,

	@Schema(description = "Access Token")
	String accessToken
) {
}
