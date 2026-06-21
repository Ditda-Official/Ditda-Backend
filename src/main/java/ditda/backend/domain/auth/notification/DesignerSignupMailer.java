package ditda.backend.domain.auth.notification;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerSignupMailer {

	private static final String SUBJECT = "[DITDA] 새 디자이너 가입 검토 요청";
	private static final String TEMPLATE = "email/designer-signup-notification";

	private final EmailSender emailSender;

	public void sendAdminNotification(
		String adminEmail,
		Long userId,
		String designerName,
		String designerEmail,
		List<String> portfolioUrls
	) {
		emailSender.send(adminEmail, SUBJECT, TEMPLATE, Map.of(
			"userId", userId,
			"name", designerName,
			"email", designerEmail,
			"portfolioUrls", portfolioUrls
		));
	}
}
