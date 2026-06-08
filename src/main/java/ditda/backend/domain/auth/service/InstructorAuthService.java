package ditda.backend.domain.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.TokenResult;
import ditda.backend.domain.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.entity.Instructor;
import ditda.backend.domain.instructor.repository.InstructorRepository;
import ditda.backend.domain.term.dto.TermAgreement;
import ditda.backend.domain.term.service.TermService;
import ditda.backend.domain.user.entity.User;
import ditda.backend.domain.user.entity.enums.UserRole;
import ditda.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructorAuthService {

	private static final String DEFAULT_PROFILE_IMAGE = "profile/default.png";

	private final InstructorRepository instructorRepository;
	private final UserService userService;
	private final TermService termService;
	private final AuthService authService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AuthResult signup(InstructorSignupRequest request) {

		userService.validateUsernameAvailable(request.username());
		userService.validateEmailAvailable(request.email());

		User user = userService.createUser(
			request.username(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.email(),
			DEFAULT_PROFILE_IMAGE,
			request.phone(),
			UserRole.INSTRUCTOR,
			LocalDateTime.now()
		);

		termService.saveTerms(user, toAgreements(request.terms()));

		// 왜인지 모르겠지만 save로 했을때는 user만 저장. Instructor는 저장 X
		instructorRepository.saveAndFlush(Instructor.createInstructor(user));

		TokenResult tokens = authService.issueTokens(user.getId());

		return new AuthResult(
			user.getId(),
			user.getName(),
			user.getProfileImage(),
			tokens.accessToken(),
			tokens.refreshToken()
		);
	}

	private List<TermAgreement> toAgreements(List<InstructorSignupRequest.TermRequest> terms) {
		return terms.stream()
			.map(t -> new TermAgreement(t.type(), t.version(), t.isAgreed()))
			.toList();
	}
}
