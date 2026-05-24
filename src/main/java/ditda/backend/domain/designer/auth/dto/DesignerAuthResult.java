package ditda.backend.domain.designer.auth.dto;

import org.springframework.http.ResponseCookie;

public record DesignerAuthResult(

	Long userId,

	String accessToken,

	ResponseCookie refreshTokenCookie
) {
}
