package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.DraftSelectedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
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

	private void registerSelectedDesigner(DraftSelectedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			event.selectedDesigner().email(),
			"[DITDA] 제출하신 1차 시안이 최종 선택되었습니다.",
			"email/first-draft-selected-designer",
			Map.of(
				"designerName", event.selectedDesigner().name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}

	private void registerRejectedDesigner(
		DraftSelectedEvent event,
		DraftSelectedEvent.DesignerInfo rejected,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			rejected.email(),
			"[DITDA] 1차 시안 선정 결과를 안내해 드립니다.",
			"email/first-draft-rejected-designer",
			Map.of(
				"designerName", rejected.name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}
}

