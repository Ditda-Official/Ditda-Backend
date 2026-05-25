package ditda.backend.global.jwt.dto;

public record RefreshTokenPayload(

	Long userId,

	String sessionId
) {
}
