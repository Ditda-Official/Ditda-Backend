package ditda.backend.domain.auth.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.event.DesignerSignedUpEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerSignupNotifier {

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;

	@EventListener
	public void onDesignerSignedUp(DesignerSignedUpEvent event) {

		// 포트폴리오가 없으면 메일 전송 X
		if (!event.hasPortfolio()) {
			return;
		}

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerAdminSignupReview(event, mailScheduledAt);

	}

	// 어드민 디자이너 회원가입 확인 메일 발송
	private void registerAdminSignupReview(DesignerSignedUpEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			NotificationType.DESIGNER_SIGNUP_REVIEW_ADMIN,
			Map.of(
				"designerId", event.designerId(),
				"name", event.name(),
				"email", event.email()
			),
			mailScheduledAt
		));
	}
}
