package ditda.backend.domain.common.auth.service;

import java.time.LocalDateTime;

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
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserEntityRepository userEntityRepository;
	private final PasswordEncoder passwordEncoder;
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
	public ResponseCookie logout(Long userId) {

		refreshTokenRepository.deleteByUserId(userId);

		return cookieUtils.deleteRefreshTokenCookie();
	}

	@Transactional
	public AuthResult reissue(String refreshToken) {

		if (refreshToken == null || refreshToken.isBlank()) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		Long userId;
		try {
			userId = Long.parseLong(
				jwtTokenProvider.validateRefreshToken(refreshToken).getSubject());
		} catch (JwtException | IllegalArgumentException exception) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		RefreshToken stored = refreshTokenRepository.findByUserId(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_TOKEN));

		if (!stored.getToken().equals(refreshToken)) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		return issueTokens(userId);
	}

	private AuthResult issueTokens(Long userId) {

		String accessToken = jwtTokenProvider.generateAccessToken(userId);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

		LocalDateTime expiresAt = jwtTokenProvider.getExpiration(refreshToken);

		refreshTokenRepository.findByUserId(userId)
			.ifPresentOrElse(
				rt -> rt.rotate(refreshToken, expiresAt),
				() -> refreshTokenRepository.save(
					RefreshToken.createRefreshToken(userEntityRepository.getReferenceById(userId), refreshToken,
						expiresAt)
				)
			);

		ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken);

		return new AuthResult(userId, accessToken, cookie);
	}
}
