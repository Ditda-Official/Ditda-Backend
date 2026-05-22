package ditda.backend.domain.common.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {

	private final EmailVerificationService emailVerificationService;
	private final EmailSender emailSender;

	public void requestEmailVerification(String email) {
		String code = emailVerificationService.issueCode(email);
		emailSender.sendVerificationEmail(email, code);
	}
}
