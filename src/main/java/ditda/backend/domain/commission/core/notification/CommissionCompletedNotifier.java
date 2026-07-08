package ditda.backend.domain.commission.core.notification;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.CommissionCompletedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import ditda.backend.global.email.NotificationType;
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
			NotificationType.COMMISSION_FINALIZED_INSTRUCTOR,
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
			NotificationType.COMMISSION_FINALIZED_DESIGNER,
			Map.of(
				"designerName", event.designerName(),
				"commissionTitle", event.commissionTitle(),
				"autoFinalized", event.autoFinalized()
			),
			event.mailScheduledAt()
		));
	}
}
