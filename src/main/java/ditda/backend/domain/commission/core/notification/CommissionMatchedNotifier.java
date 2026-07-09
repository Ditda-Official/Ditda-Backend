package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.CommissionMatchedEvent;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionMatchedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onCommissionMatched(CommissionMatchedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerInstructorMatchComplete(event, mailScheduledAt);

		for (CommissionMatchedEvent.DesignerMatchInfo designer : event.matchedDesigners()) {
			registerDesignerMatchComplete(event, designer, mailScheduledAt);
		}
	}

	// 강사 매칭 완료 알림 메일 발송
	private void registerInstructorMatchComplete(CommissionMatchedEvent event, LocalDateTime mailScheduledAt) {

		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.APPLICATION_MATCHED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"requiredCount", event.requiredCount(),
				"designerCount", event.matchedDesignerCount()
			),
			mailScheduledAt
		));
	}

	// 디자이너 매칭 완료 알림 메일 발송
	private void registerDesignerMatchComplete(
		CommissionMatchedEvent event,
		CommissionMatchedEvent.DesignerMatchInfo designer,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			designer.email(),
			NotificationType.APPLICATION_MATCHED_DESIGNER,
			Map.of(
				"designerName", designer.name(),
				"commissionTitle", event.commissionTitle(),
				"firstDraftDeadline", event.firstDraftDeadline()
			),
			mailScheduledAt
		));
	}

}
