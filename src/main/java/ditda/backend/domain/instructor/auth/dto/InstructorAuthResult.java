package ditda.backend.domain.instructor.auth.dto;

public record InstructorAuthResult(

	Long userId,

	String name,

	String profileImage,

	String accessToken,

	String refreshToken
) {
}
