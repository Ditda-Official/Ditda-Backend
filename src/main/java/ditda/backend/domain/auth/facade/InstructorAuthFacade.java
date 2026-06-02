package ditda.backend.domain.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.auth.service.EmailVerificationService;
import ditda.backend.domain.auth.service.InstructorAuthService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorAuthFacade {

	private final InstructorAuthService instructorAuthService;
	private final EmailVerificationService emailVerificationService;

	public AuthResult signup(InstructorSignupRequest request) {

		emailVerificationService.validateVerified(request.email());

		AuthResult result = instructorAuthService.signup(request);

		emailVerificationService.deleteVerified(request.email());

		return result;
	}

}
