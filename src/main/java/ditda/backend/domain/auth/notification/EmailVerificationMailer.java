package ditda.backend.domain.auth.notification;

import java.util.Map;

import org.springframework.stereotype.Component;

import ditda.backend.global.notification.EmailSender;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationMailer {

	private final EmailSender emailSender;

	public void sendVerificationCode(String to, String code) {
		emailSender.sendAsync(to, NotificationType.EMAIL_VERIFICATION, Map.of("code", code));
	}
}
