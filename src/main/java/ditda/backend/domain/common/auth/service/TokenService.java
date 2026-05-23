package ditda.backend.domain.common.auth.service;

import java.time.LocalDateTime;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.common.auth.dto.TokenPair;
import ditda.backend.domain.common.auth.entity.RefreshToken;
import ditda.backend.domain.common.auth.repository.RefreshTokenRepository;
import ditda.backend.domain.common.user.service.UserService;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.utils.CookieUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final CookieUtils cookieUtils;

	@Transactional
	public TokenPair issueTokens(Long userId) {
		String accessToken = jwtTokenProvider.generateAccessToken(userId);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
		LocalDateTime expiresAt = jwtTokenProvider.getExpiration(refreshToken);

		refreshTokenRepository.findByUserId(userId)
			.ifPresentOrElse(
				rt -> rt.rotate(refreshToken, expiresAt),
				() -> refreshTokenRepository.save(
					RefreshToken.createRefreshToken(
						userService.getReferenceById(userId),
						refreshToken,
						expiresAt
					)
				)
			);

		ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken);
		return new TokenPair(accessToken, cookie);
	}
}
