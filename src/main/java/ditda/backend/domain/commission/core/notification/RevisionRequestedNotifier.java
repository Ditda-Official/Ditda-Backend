package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.RevisionRequestedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevisionRequestedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onRevisionRequested(RevisionRequestedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerDesignerRevisionRequested(event, mailScheduledAt);
	}

	private void registerDesignerRevisionRequested(
		RevisionRequestedEvent event,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.designerEmail(),
			"[DITDA] 시안 수정 요청이 도착했습니다. 확인해주세요.",
			"email/revision-requested-designer",
			Map.of(
				"designerName", event.designerName(),
				"commissionTitle", event.commissionTitle(),
				"currentRevisionCount", event.currentRevisionCount()
			),
			mailScheduledAt
		));
	}
}
