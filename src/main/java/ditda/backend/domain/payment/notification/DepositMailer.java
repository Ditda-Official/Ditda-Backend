package ditda.backend.domain.payment.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import ditda.backend.global.email.EmailSender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositMailer {

	private static final String SUBJECT = "[DITDA] 외주 입금 확인 요청";
	private static final String TEMPLATE = "email/deposit-notification";

	private final EmailSender emailSender;

	public void sendAdminNotification(
		String adminEmail,
		Long commissionId,
		String commissionTitle,
		String instructorName,
		String depositorName,
		int amount,
		LocalDateTime notifiedAt
	) {
		emailSender.send(adminEmail, SUBJECT, TEMPLATE, Map.of(
			"commissionId", commissionId,
			"title", commissionTitle,
			"instructorName", instructorName,
			"depositorName", depositorName,
			"amount", amount,
			"notifiedAt", notifiedAt
		));
	}
}
