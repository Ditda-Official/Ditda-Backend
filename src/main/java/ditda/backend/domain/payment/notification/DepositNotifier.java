package ditda.backend.domain.payment.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.payment.event.DepositNotifiedEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositNotifier {

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;

	@EventListener
	public void onDepositNotified(DepositNotifiedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerAdminDepositCheck(event, mailScheduledAt);
	}

	// 어드민 입금 확인 메일 발송
	private void registerAdminDepositCheck(DepositNotifiedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			NotificationType.DEPOSIT_CONFIRM_REQUEST_ADMIN,
			Map.of(
				"commissionId", event.commissionId(),
				"title", event.commissionTitle(),
				"instructorName", event.instructorName(),
				"depositorName", event.depositorName(),
				"amount", event.amount(),
				"notifiedAt", event.notifiedAt()
			),
			mailScheduledAt
		));
	}
}
