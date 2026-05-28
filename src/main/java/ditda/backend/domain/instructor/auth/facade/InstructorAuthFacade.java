package ditda.backend.domain.instructor.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.auth.service.InstructorAuthService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorAuthFacade {

	private final InstructorAuthService instructorAuthService;
	private final EmailVerificationService emailVerificationService;

	public InstructorAuthResult signup(InstructorSignupRequest request) {

		emailVerificationService.validateVerified(request.email());

		InstructorAuthResult result = instructorAuthService.signup(request);

		emailVerificationService.deleteVerified(request.email());

		return result;
	}

}
