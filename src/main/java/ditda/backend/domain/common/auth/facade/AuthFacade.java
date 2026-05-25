package ditda.backend.domain.common.auth.facade;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.dto.AuthResult;
import ditda.backend.domain.common.auth.dto.request.LoginRequest;
import ditda.backend.domain.common.auth.service.AuthService;
import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {

	private final EmailVerificationService emailVerificationService;
	private final EmailSender emailSender;
	private final AuthService authService;

	// 이메일 인증번호 발송
	public void requestEmailVerification(String email) {
		String code = emailVerificationService.issueCode(email);
		emailSender.sendVerificationEmail(email, code);
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
	public ResponseCookie logout(String refreshToken) {
		return authService.logout(refreshToken);
	}

	// 토큰 재발급
	public AuthResult reissue(String refreshToken) {
		return authService.reissue(refreshToken);
	}
}
