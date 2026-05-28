package ditda.backend.domain.designer.auth.dto;

import org.springframework.http.ResponseCookie;

public record DesignerAuthResult(

	Long userId,

	String name,

	String profileImage,

	String accessToken,

	ResponseCookie refreshTokenCookie
) {
}
