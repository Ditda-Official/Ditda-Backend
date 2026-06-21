package ditda.backend.domain.auth.dto;

public record TokenResult(
	String accessToken,
	String refreshToken
) {
}
