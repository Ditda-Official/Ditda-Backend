package ditda.backend.domain.common.auth.dto;

public record TokenResult(
	String accessToken,
	String refreshToken
) {
}
