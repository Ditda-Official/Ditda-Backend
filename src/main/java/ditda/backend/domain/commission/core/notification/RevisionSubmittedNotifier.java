package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.RevisionSubmittedEvent;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevisionSubmittedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onRevisionSubmitted(RevisionSubmittedEvent event) {

		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerInstructorRevisionSubmitted(event, mailScheduledAt);
	}

	// 강사 시안 수정본 제출됨 메일 발송
	private void registerInstructorRevisionSubmitted(
		RevisionSubmittedEvent event,
		LocalDateTime mailScheduledAt
	) {

		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.REVISION_SUBMITTED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"currentRevisionCount", event.currentRevisionCount()
			),
			mailScheduledAt
		));
	}
}
