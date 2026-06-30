package ditda.backend.domain.commission.core.notification;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.CommissionCompletedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionCompletedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onCommissionCompleted(CommissionCompletedEvent event) {
		registerInstructorFinalized(event);
		registerDesignerFinalized(event);
	}

	// 강사 외주 최종 확정 안내
	private void registerInstructorFinalized(CommissionCompletedEvent event) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			"[DITDA] 신청하신 외주가 최종 확정되었습니다.",
			"email/commission-finalized-instructor",
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"autoFinalized", event.autoFinalized()
			),
			event.mailScheduledAt()
		));
	}

	// 디자이너 외주 최종 확정 안내
	private void registerDesignerFinalized(CommissionCompletedEvent event) {
		outboxRepository.save(NotificationOutbox.create(
			event.designerEmail(),
			"[DITDA] 작업하신 외주가 최종 확정되었습니다.",
			"email/commission-finalized-designer",
			Map.of(
				"designerName", event.designerName(),
				"commissionTitle", event.commissionTitle(),
				"autoFinalized", event.autoFinalized()
			),
			event.mailScheduledAt()
		));
	}
}
