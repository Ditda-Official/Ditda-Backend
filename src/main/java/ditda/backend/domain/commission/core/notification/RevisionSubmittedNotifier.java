package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.RevisionSubmittedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
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

	private void registerInstructorRevisionSubmitted(
		RevisionSubmittedEvent event,
		LocalDateTime mailScheduledAt
	) {

		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			"[DITDA] 수정본이 제출되었습니다. 확인해주세요.",
			"email/revision-submitted-instructor",
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"currentRevisionCount", event.currentRevisionCount()
			),
			mailScheduledAt
		));
	}
}
