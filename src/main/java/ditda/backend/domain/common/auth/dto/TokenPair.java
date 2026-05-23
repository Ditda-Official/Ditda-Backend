package ditda.backend.domain.common.auth.dto;

import org.springframework.http.ResponseCookie;

public record TokenPair(
	String accessToken,
	ResponseCookie refreshCookie
) {
}
