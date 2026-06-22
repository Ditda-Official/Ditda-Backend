package ditda.backend.domain.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.TokenResult;
import ditda.backend.domain.auth.dto.request.LoginRequest;
import ditda.backend.domain.auth.entity.RefreshToken;
import ditda.backend.domain.auth.repository.RefreshTokenRepository;
import ditda.backend.domain.user.entity.User;
import ditda.backend.domain.user.service.UserService;
import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.hash.RefreshTokenHasher;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.dto.RefreshTokenPayload;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenHasher refreshTokenHasher;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public TokenResult issueTokens(User user) {

		Long userId = user.getId();

		// 1. 만료된 토큰 삭제
		refreshTokenRepository.deleteExpiredByUserId(userId, LocalDateTime.now());

		// 2. sessionId(로그인 기기 식별) 및 JWT 토큰 발급
		String sessionId = UUID.randomUUID().toString();
		String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getRole());
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);

		// 3. DB에는 해시 값으로 저장
		String refreshTokenHash = refreshTokenHasher.hash(refreshToken);
		LocalDateTime expiresAt = jwtTokenProvider.getExpiration(refreshToken);

		refreshTokenRepository.save(RefreshToken.createRefreshToken(user, sessionId, refreshTokenHash, expiresAt));

		return new TokenResult(accessToken, refreshToken);
	}

	@Transactional
	public AuthResult login(LoginRequest request) {

		// 1. 유저 조회
		User user = userService.findByUsername(request.username());

		// 2. 비밀번호 검증
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
		}

		// 3. Access / Refresh 토큰 발급
		TokenResult tokens = issueTokens(user);

		return new AuthResult(
			user.getId(),
			user.getRole(),
			user.getName(),
			user.getProfileImage(),
			tokens.accessToken(),
			tokens.refreshToken()
		);
	}

	@Transactional
	public void logout(String refreshToken) {

		if (refreshToken != null && !refreshToken.isBlank()) {
			try {
				// 1. Refresh Token 검증 후 payload 추출
				RefreshTokenPayload payload = jwtTokenProvider.getRefreshTokenPayload(refreshToken);

				// 2. 전달받은 Refresh Token을 저장 형식과 동일하게 해싱
				String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

				// 3. sessionId로 저장된 토큰을 조회하고, 해시가 일치하는 경우에만 삭제
				refreshTokenRepository.findBySessionId(payload.sessionId())
					.filter(stored -> stored.matchesHash(refreshTokenHash))
					.ifPresent(refreshTokenRepository::delete);
			} catch (JwtException | IllegalArgumentException exception) {
				// 토큰이 만료 또는 위조여도 쿠키는 제거
			}
		}
	}

	@Transactional
	public TokenResult reissue(String refreshToken) {

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
		User user = userService.findById(userId);
		String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getRole());
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, sessionId);
		String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);
		LocalDateTime newExpiresAt = jwtTokenProvider.getExpiration(newRefreshToken);

		stored.rotate(newRefreshTokenHash, newExpiresAt);

		return new TokenResult(newAccessToken, newRefreshToken);
	}

	@Transactional
	public int deleteExpiredRefreshTokens() {
		return refreshTokenRepository.deleteExpired(LocalDateTime.now());
	}

}
