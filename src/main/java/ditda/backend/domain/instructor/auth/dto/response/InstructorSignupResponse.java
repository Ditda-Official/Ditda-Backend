package ditda.backend.domain.instructor.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 회원가입 응답")
public record InstructorSignupResponse(

	@Schema(description = "User ID")
	Long userId,

	@Schema(description = "Access Token")
	String accessToken
) {
}
