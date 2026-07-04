package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.AllFirstDraftsSubmittedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
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

	private void registerInstructorSelectionRequest(
		AllFirstDraftsSubmittedEvent event, LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			"[DITDA] 모든 1차 시안이 제출되었습니다. 시안을 선택해 주세요.",
			"email/first-draft-all-submitted-instructor",
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"submittedCount", event.submittedCount()
			),
			mailScheduledAt
		));
	}
}
