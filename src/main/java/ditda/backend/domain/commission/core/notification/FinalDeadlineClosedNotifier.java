package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.FinalDeadlineClosedEvent;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import ditda.backend.global.email.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinalDeadlineClosedNotifier {

	private final NotificationOutboxRepository outboxRepository;

	@EventListener
	public void onFinalDeadlineClosed(FinalDeadlineClosedEvent event) {

		// CASE 2는 CommissionCompletedEvent가 담당
		if (!event.cancelled()) {
			return;
		}

		// CASE 1: 강사 시안 미선택 -> 외주 취소
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		registerInstructorCancellation(event, mailScheduledAt);

		for (FinalDeadlineClosedEvent.DesignerInfo d : event.submittedDesigners()) {
			registerDesignerCancellation(event, d, mailScheduledAt);
		}
	}

	// 강사 시안 미선택으로 취소 안내 메일 발송
	private void registerInstructorCancellation(
		FinalDeadlineClosedEvent event, LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.FINAL_CANCELLED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}

	// 디자이너 강사 미선택으로 취소 안내 메일 발송
	private void registerDesignerCancellation(
		FinalDeadlineClosedEvent event,
		FinalDeadlineClosedEvent.DesignerInfo designer,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			designer.email(),
			NotificationType.FINAL_CANCELLED_DESIGNER,
			Map.of(
				"designerName", designer.name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}

}
