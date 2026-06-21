package ditda.backend.domain.auth.dto.response;

import ditda.backend.domain.user.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignupResponse(

	@Schema(description = "User ID")
	Long userId,

	@Schema(description = "사용자 타입", example = "INSTRUCTOR")
	UserRole userType,

	@Schema(description = "사용자 이름", example = "홍길동")
	String name,

	@Schema(description = "프로필 이미지", example = "https://example.com/profile/default.png")
	String profileImageUrl,

	@Schema(description = "Access Token")
	String accessToken
) {
}
