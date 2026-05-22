package ditda.backend.global.jwt.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

	public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	@Value("${jwt.refresh-token-expiration}")
	private Long refreshTokenExpiration;

	@Value("${jwt.refresh-cookie-path}")
	private String refreshCookiePath;

	@Value("${jwt.cookie-secure}")
	private boolean secure;

	public ResponseCookie createRefreshTokenCookie(String token) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
			.httpOnly(true)
			.secure(secure)
			.sameSite("Lax")
			.path(refreshCookiePath)
			.maxAge(refreshTokenExpiration / 1000)
			.build();
	}

	public ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
			.httpOnly(true)
			.secure(secure)
			.sameSite("Lax")
			.path(refreshCookiePath)
			.maxAge(0)
			.build();
	}
}
