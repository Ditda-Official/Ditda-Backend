package ditda.backend.domain.instructor.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.common.auth.entity.RefreshToken;
import ditda.backend.domain.common.auth.repository.RefreshTokenRepository;
import ditda.backend.domain.common.term.entity.UserTerm;
import ditda.backend.domain.common.term.entity.enums.TermType;
import ditda.backend.domain.common.term.repository.UserTermRepository;
import ditda.backend.domain.common.user.entity.UserEntity;
import ditda.backend.domain.common.user.entity.enums.UserRole;
import ditda.backend.domain.common.user.exceptions.UserErrorCode;
import ditda.backend.domain.common.user.repository.UserEntityRepository;
import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.auth.entity.Instructor;
import ditda.backend.domain.instructor.auth.repository.InstructorRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.hash.RefreshTokenHasher;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.utils.CookieUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructorAuthService {

	private static final String DEFAULT_PROFILE_IMAGE = "";
	private static final Set<TermType> REQUIRED_TERMS = Set.of(
		TermType.SERVICE,
		TermType.USERINFO,
		TermType.SETTLEMENT,
		TermType.DISINTERMEDIATION
	);

	private final UserEntityRepository userEntityRepository;
	private final InstructorRepository instructorRepository;
	private final UserTermRepository userTermRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenHasher refreshTokenHasher;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final CookieUtils cookieUtils;

	@Transactional(readOnly = true)
	public void validateUsernameAvailable(String username) {

		if (userEntityRepository.existsByUsername(username)) {
			throw new GeneralException(UserErrorCode.USERNAME_ALREADY_EXISTS);
		}
	}

	@Transactional
	public InstructorAuthResult signup(InstructorSignupRequest request) {

		validateUsernameAvailable(request.username());
		validateEmailAvailable(request);
		validateRequiredTerms(request.terms());

		UserEntity user = UserEntity.createUser(
			request.username(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.email(),
			DEFAULT_PROFILE_IMAGE,
			request.phone(),
			UserRole.INSTRUCTOR,
			LocalDateTime.now()
		);

		userEntityRepository.save(user);

		List<UserTerm> terms = request.terms().stream()
			.map(term -> UserTerm.createTerm(
				user,
				term.type(),
				term.version(),
				term.isAgreed()
			))
			.toList();

		userTermRepository.saveAll(terms);

		instructorRepository.save(Instructor.createInstructor(user));

		return issueTokens(user.getId());
	}

	private void validateEmailAvailable(InstructorSignupRequest request) {

		if (userEntityRepository.existsByEmail(request.email())) {
			throw new GeneralException(UserErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	private void validateRequiredTerms(List<InstructorSignupRequest.TermRequest> terms) {

		for (TermType requiredTerm : REQUIRED_TERMS) {
			boolean isAgreed = terms.stream()
				.anyMatch(term -> term.type() == requiredTerm && term.isAgreed());

			if (!isAgreed) {
				throw new GeneralException(UserErrorCode.USER_NOT_AGREED_TERMS);
			}
		}
	}

	private InstructorAuthResult issueTokens(Long userId) {

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

		return new InstructorAuthResult(userId, accessToken, cookie);
	}
}
