package ditda.backend.global.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ditda.backend.global.jwt.dto.AccessTokenPayload;
import ditda.backend.global.jwt.dto.RefreshTokenPayload;
import ditda.backend.global.jwt.enums.AuthRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {

	private static final String SESSION_ID_CLAIM = "sid";
	private static final String TOKEN_TYPE_CLAIM = "type";
	private static final String ROLE_CLAIM = "role";
	private static final String ACCESS_TOKEN_TYPE = "access_token";
	private static final String REFRESH_TOKEN_TYPE = "refresh_token";

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration}")
	private Long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private Long refreshTokenExpiration;

	private SecretKey key;

	@PostConstruct
	public void init() {

		byte[] keyBytes = Decoders.BASE64URL.decode(secret);
		key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(Long userId, AuthRole role) {

		Date now = new Date();
		Date expiryTime = new Date(now.getTime() + accessTokenExpiration);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
			.claim(ROLE_CLAIM, role.name())
			.issuedAt(now)
			.expiration(expiryTime)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}

	public String generateRefreshToken(Long userId, String sessionId) {

		Date now = new Date();
		Date expiryTime = new Date(now.getTime() + refreshTokenExpiration);

		return Jwts.builder()
			.subject(userId.toString())
			.claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
			.claim(SESSION_ID_CLAIM, sessionId)
			.issuedAt(now)
			.expiration(expiryTime)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}

	public AccessTokenPayload getAccessTokenPayload(String token) {

		Claims claims = validateAccessToken(token);

		Long userId = Long.parseLong(claims.getSubject());
		AuthRole role = getRole(claims);

		return new AccessTokenPayload(userId, role);
	}

	public RefreshTokenPayload getRefreshTokenPayload(String token) {

		Claims claims = validateRefreshToken(token);

		Long userId = Long.parseLong(claims.getSubject());
		String sessionId = getSessionId(claims);

		return new RefreshTokenPayload(userId, sessionId);
	}

	public LocalDateTime getExpiration(String token) {

		return getClaims(token).getExpiration()
			.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDateTime();
	}

	private String getSessionId(Claims claims) {

		String sessionId = claims.get(SESSION_ID_CLAIM, String.class);

		if (sessionId == null || sessionId.isBlank()) {
			throw new JwtException("Refresh Token에 세션 정보가 없습니다.");
		}

		return sessionId;
	}

	private Claims validateAccessToken(String token) {

		Claims claims = validateToken(token);
		if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
			throw new JwtException("Access Token이 아닙니다.");
		}

		return claims;
	}

	private Claims validateRefreshToken(String token) {

		Claims claims = validateToken(token);
		if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
			throw new JwtException("Refresh Token이 아닙니다.");
		}

		return claims;
	}

	private Claims getClaims(String token) {

		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private AuthRole getRole(Claims claims) {

		String role = claims.get(ROLE_CLAIM, String.class);

		if (role == null || role.isBlank()) {
			throw new JwtException("Access Token에 role 정보가 없습니다.");
		}

		return AuthRole.valueOf(role);
	}

	private Claims validateToken(String token) {

		try {
			return getClaims(token);
		} catch (ExpiredJwtException e) {
			log.warn("만료된 JWT 토큰: {}", e.getMessage());
			throw e;
		} catch (UnsupportedJwtException e) {
			log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
			throw e;
		} catch (MalformedJwtException e) {
			log.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
			throw e;
		} catch (SignatureException e) {
			log.warn("JWT 서명 검증 실패: {}", e.getMessage());
			throw e;
		} catch (IllegalArgumentException e) {
			log.warn("JWT 토큰이 비어있음: {}", e.getMessage());
			throw e;
		}
	}
}
