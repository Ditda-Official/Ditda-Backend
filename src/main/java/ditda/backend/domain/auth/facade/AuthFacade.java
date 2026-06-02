package ditda.backend.domain.auth.facade;

import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.TokenResult;
import ditda.backend.domain.auth.dto.request.LoginRequest;
import ditda.backend.domain.auth.notification.EmailVerificationMailer;
import ditda.backend.domain.auth.service.AuthService;
import ditda.backend.domain.auth.service.EmailVerificationService;
import ditda.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {

	private final EmailVerificationService emailVerificationService;
	private final EmailVerificationMailer emailVerificationMailer;
	private final UserService userService;
	private final AuthService authService;

	// 아이디 중복 검증
	public void validateUsernameAvailable(String username) {
		userService.validateUsernameAvailable(username);
	}

	// 이메일 인증번호 발송
	public void requestEmailVerification(String email) {
		String code = emailVerificationService.issueCode(email);
		emailVerificationMailer.sendVerificationCode(email, code);
	}

	// 이메일 인증번호 검증
	public void verifyEmailCode(String email, String code) {
		emailVerificationService.verifyCode(email, code);
	}

	// 유저 로그인
	public AuthResult login(LoginRequest request) {
		return authService.login(request);
	}

	// 유저 로그아웃
	public void logout(String refreshToken) {
		authService.logout(refreshToken);
	}

	// 토큰 재발급
	public TokenResult reissue(String refreshToken) {
		return authService.reissue(refreshToken);
	}
}
