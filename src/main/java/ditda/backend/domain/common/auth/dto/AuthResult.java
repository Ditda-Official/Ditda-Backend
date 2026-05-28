package ditda.backend.domain.common.auth.dto;

public record AuthResult(

	Long userId,

	String name,

	String profileImage,

	String accessToken,

	String refreshToken
) {
}
