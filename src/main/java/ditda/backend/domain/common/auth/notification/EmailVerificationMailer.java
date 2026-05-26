package ditda.backend.domain.common.auth.notification;

import java.util.Map;

import org.springframework.stereotype.Component;

import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationMailer {

	private static final String SUBJECT = "[DITDA] 이메일 인증 코드";
	private static final String TEMPLATE = "email/verification-code";

	private final EmailSender emailSender;

	public void sendVerificationCode(String to, String code) {
		emailSender.send(to, SUBJECT, TEMPLATE, Map.of("code", code));
	}
}
