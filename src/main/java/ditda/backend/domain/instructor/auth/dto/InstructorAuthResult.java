package ditda.backend.domain.instructor.auth.dto;

import org.springframework.http.ResponseCookie;

public record InstructorAuthResult(

	Long userId,

	String accessToken,

	ResponseCookie refreshTokenCookie
) {
}
