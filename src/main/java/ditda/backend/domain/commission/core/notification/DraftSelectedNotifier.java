package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.DraftSelectedEvent;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftSelectedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onDraftSelected(DraftSelectedEvent event) {

		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		// 선택된 디자이너 안내
		registerSelectedDesigner(event, mailScheduledAt);

		// 미선택 디자이너 안내
		for (DraftSelectedEvent.DesignerInfo rejected : event.rejectedDesigners()) {
			registerRejectedDesigner(event, rejected, mailScheduledAt);
		}
	}

	// 디자이너 시안 선택됨 메일 발송
	private void registerSelectedDesigner(DraftSelectedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			event.selectedDesigner().email(),
			NotificationType.DRAFT_SELECTED_DESIGNER,
			Map.of(
				"designerName", event.selectedDesigner().name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}

	// 디자이너 시안 선택되지 않음 메일 발송
	private void registerRejectedDesigner(
		DraftSelectedEvent event,
		DraftSelectedEvent.DesignerInfo rejected,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			rejected.email(),
			NotificationType.DRAFT_REJECTED_DESIGNER,
			Map.of(
				"designerName", rejected.name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}
}

