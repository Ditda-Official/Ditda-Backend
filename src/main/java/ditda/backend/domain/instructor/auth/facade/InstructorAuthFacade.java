package ditda.backend.domain.instructor.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.exception.AuthErrorCode;
import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.auth.service.InstructorAuthService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorAuthFacade {

	private final InstructorAuthService instructorAuthService;
	private final EmailVerificationService emailVerificationService;

	public InstructorAuthResult signup(InstructorSignupRequest request) {

		// 이메일 검증 여부 확인
		if (!emailVerificationService.isVerified(request.email())) {
			throw new GeneralException(AuthErrorCode.EMAIL_NOT_VERIFIED);
		}

		InstructorAuthResult result = instructorAuthService.signup(request);

		emailVerificationService.deleteVerified(request.email());

		return result;
	}

	public void validateUsernameAvailable(String username) {
		instructorAuthService.validateUsernameAvailable(username);
	}
}
