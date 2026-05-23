package ditda.backend.domain.instructor.auth.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.common.auth.dto.TokenPair;
import ditda.backend.domain.common.auth.service.TokenService;
import ditda.backend.domain.common.term.dto.TermAgreement;
import ditda.backend.domain.common.term.service.TermService;
import ditda.backend.domain.common.user.entity.UserEntity;
import ditda.backend.domain.common.user.entity.enums.UserRole;
import ditda.backend.domain.common.user.service.UserService;
import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.auth.entity.Instructor;
import ditda.backend.domain.instructor.auth.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructorAuthService {

	private static final String DEFAULT_PROFILE_IMAGE = "";

	private final InstructorRepository instructorRepository;
	private final UserService userService;
	private final TermService termService;
	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public InstructorAuthResult signup(InstructorSignupRequest request) {

		userService.validateUsernameAvailable(request.username());
		userService.validateEmailAvailable(request.email());

		UserEntity user = userService.createUser(
			request.username(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.email(),
			DEFAULT_PROFILE_IMAGE,
			request.phone(),
			UserRole.INSTRUCTOR
		);

		termService.saveTerms(user, toAgreements(request.terms()));

		instructorRepository.save(Instructor.createInstructor(user));

		TokenPair tokens = tokenService.issueTokens(user.getId());

		return new InstructorAuthResult(user.getId(), tokens.accessToken(), tokens.refreshCookie());
	}

	private List<TermAgreement> toAgreements(List<InstructorSignupRequest.TermRequest> terms) {
		return terms.stream()
			.map(t -> new TermAgreement(t.type(), t.version(), t.isAgreed()))
			.toList();
	}
}
