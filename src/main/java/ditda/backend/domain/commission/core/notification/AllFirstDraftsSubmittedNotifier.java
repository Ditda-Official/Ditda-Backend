package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.AllFirstDraftsSubmittedEvent;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllFirstDraftsSubmittedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onAllFirstDraftsSubmitted(AllFirstDraftsSubmittedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerInstructorSelectionRequest(event, mailScheduledAt);
	}

	// 강사 1차 시안 전원 제출 메일 발송
	private void registerInstructorSelectionRequest(
		AllFirstDraftsSubmittedEvent event, LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.ALL_FIRST_DRAFTS_SUBMITTED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"submittedCount", event.submittedCount()
			),
			mailScheduledAt
		));
	}
}
