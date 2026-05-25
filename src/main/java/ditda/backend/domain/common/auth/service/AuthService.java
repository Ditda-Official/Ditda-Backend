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
import ditda.backend.global.hash.RefreshTokenHasher;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.dto.RefreshTokenPayload;
import ditda.backend.global.jwt.utils.CookieUtils;
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

		// 1. 유저 조회
		UserEntity user = userEntityRepository.findByUsername(request.username())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_LOGIN));

		// 2. 비밀번호 검증
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
		}

		// 3. Access / Refresh 토큰 발급
		return issueTokens(user.getId());
	}

	@Transactional
	public ResponseCookie logout(String refreshToken) {

		if (refreshToken != null && !refreshToken.isBlank()) {
			try {
				// 1. JWT 토큰 payload
				RefreshTokenPayload payload = jwtTokenProvider.getRefreshTokenPayload(refreshToken);

				refreshTokenRepository.deleteBySessionId(payload.sessionId());
			} catch (JwtException | IllegalArgumentException exception) {
				// 토큰이 만료 또는 위조여도 쿠키는 제거
			}
		}

		return cookieUtils.deleteRefreshTokenCookie();
	}

	@Transactional
	public AuthResult reissue(String refreshToken) {

		// 1. 쿠키 존재 여부 확인
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		// 2. JWT 서명/만료 검증 후 userId, sessionId 추출
		Long userId;
		String sessionId;
		try {
			RefreshTokenPayload payload = jwtTokenProvider.getRefreshTokenPayload(refreshToken);
			userId = payload.userId();
			sessionId = payload.sessionId();
		} catch (JwtException | IllegalArgumentException exception) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		// 3. sessionId로 DB의 토큰 조회
		RefreshToken stored = refreshTokenRepository.findBySessionId(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_TOKEN));

		// 4. 소유자 일치 + 해시 일치 검증
		String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

		if (!stored.belongsTo(userId) || !stored.matchesHash(refreshTokenHash)) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
		}

		// 5. 새 Access / Refresh 토큰 발급. (sessionId는 유지)
		String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);
		String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);
		LocalDateTime newExpiresAt = jwtTokenProvider.getExpiration(newRefreshToken);

		stored.rotate(newRefreshTokenHash, newExpiresAt);

		ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(newRefreshToken);

		return new AuthResult(userId, newAccessToken, cookie);
	}

	@Transactional
	public int deleteExpiredRefreshTokens() {
		return refreshTokenRepository.deleteExpired(LocalDateTime.now());
	}

	private AuthResult issueTokens(Long userId) {

		// 1. 만료된 토큰 삭제
		refreshTokenRepository.deleteExpiredByUserId(userId, LocalDateTime.now());

		// 2. sessionId(로그인 기기 식별) 및 JWT 토큰 발급
		String sessionId = UUID.randomUUID().toString();
		String accessToken = jwtTokenProvider.generateAccessToken(userId);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);

		// 3. DB에는 해시 값으로 저장
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
