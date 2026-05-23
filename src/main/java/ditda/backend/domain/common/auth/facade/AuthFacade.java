package ditda.backend.domain.common.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.domain.common.user.service.UserService;
import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {

	private final EmailVerificationService emailVerificationService;
	private final EmailSender emailSender;
	private final UserService userService;

	// 아이디 중복 검증
	public void validateUsernameAvailable(String username) {
		userService.validateUsernameAvailable(username);
	}

	// 이메일 인증번호 발송
	public void requestEmailVerification(String email) {
		String code = emailVerificationService.issueCode(email);
		emailSender.sendVerificationEmail(email, code);
	}

	// 이메일 인증번호 검증
	public void verifyEmailCode(String email, String code) {
		emailVerificationService.verifyCode(email, code);
	}
}
