package ditda.backend.domain.common.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.common.auth.dto.AuthResult;
import ditda.backend.domain.common.auth.dto.request.LoginRequest;
import ditda.backend.domain.common.auth.entity.RefreshToken;
import ditda.backend.domain.common.auth.repository.RefreshTokenRepository;
import ditda.backend.domain.common.user.entity.UserEntity;
import ditda.backend.domain.common.user.repository.UserEntityRepository;
import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserEntityRepository userEntityRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenHasher refreshTokenHasher;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final CookieUtils cookieUtils;

	@Transactional
	public AuthResult login(LoginRequest request) {

		UserEntity user = userEntityRepository.findByUsername(request.username())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_LOGIN));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
		}

		return issueTokens(user.getId());
	}

	@Transactional
	public ResponseCookie logout(Long userId, String refreshToken) {

		if (refreshToken != null && !refreshToken.isBlank()) {
			try {
				Claims claims = jwtTokenProvider.validateRefreshToken(refreshToken);
				Long tokenUserId = Long.parseLong(claims.getSubject());
				String sessionId = jwtTokenProvider.getSessionId(claims);

				if (userId.equals(tokenUserId)) {
					refreshTokenRepository.deleteBySessionId(sessionId);
				}
			} catch (JwtException | IllegalArgumentException exception) {
				// 쿠키 삭제는 진행.
			}
		}

		return cookieUtils.deleteRefreshTokenCookie();
	}

	@Transactional
	public AuthResult reissue(String refreshToken) {

		if (refreshToken == null || refreshToken.isBlank()) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		Long userId;
		String sessionId;
		try {
			Claims claims = jwtTokenProvider.validateRefreshToken(refreshToken);
			userId = Long.parseLong(claims.getSubject());
			sessionId = jwtTokenProvider.getSessionId(claims);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		RefreshToken stored = refreshTokenRepository.findBySessionId(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_TOKEN));

		String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

		if (!stored.belongsTo(userId) || !stored.matchesHash(refreshTokenHash)) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);
		String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);
		LocalDateTime newExpiresAt = jwtTokenProvider.getExpiration(newRefreshToken);

		stored.rotate(newRefreshTokenHash, newExpiresAt);

		ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(newRefreshToken);

		return new AuthResult(userId, newAccessToken, cookie);
	}

	private AuthResult issueTokens(Long userId) {

		// 만료된 토큰 삭제
		refreshTokenRepository.deleteExpiredByUserId(userId, LocalDateTime.now());

		String sessionId = UUID.randomUUID().toString();

		String accessToken = jwtTokenProvider.generateAccessToken(userId);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);
		String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

		LocalDateTime expiresAt = jwtTokenProvider.getExpiration(refreshToken);

		refreshTokenRepository.save(
			RefreshToken.createRefreshToken(
				userEntityRepository.getReferenceById(userId),
				sessionId,
				refreshTokenHash,
				expiresAt
			)
		);

		ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken);

		return new AuthResult(userId, accessToken, cookie);
	}
}
