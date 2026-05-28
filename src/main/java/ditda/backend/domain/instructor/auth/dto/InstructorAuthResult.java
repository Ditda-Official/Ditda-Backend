package ditda.backend.domain.instructor.auth.dto;

public record InstructorAuthResult(

	Long userId,

	String accessToken,

	String refreshToken
) {
}
