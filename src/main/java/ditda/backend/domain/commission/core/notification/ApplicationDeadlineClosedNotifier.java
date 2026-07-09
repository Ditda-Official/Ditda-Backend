package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.ApplicationDeadlineClosedEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.notification.NotificationOutbox;
import ditda.backend.global.notification.NotificationOutboxRepository;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationDeadlineClosedNotifier {

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;

	@EventListener
	public void onApplicationDeadlineClosed(ApplicationDeadlineClosedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		// 지원자 0명 -> 외주 취소 + 강사 취소 메일 + 어드민 환불 요청
		if (event.cancelled()) {
			registerAdminRefundRequest(event, mailScheduledAt);
			registerInstructorCancellation(event, mailScheduledAt);
			return;
		}

		// 정원 미달 -> 외주 진행 + 어드민 환불 요청
		if (event.refundAmount() > 0) {
			registerAdminRefundRequest(event, mailScheduledAt);
			registerInstructorShortfall(event, mailScheduledAt);
		} else {
			// 정원 충족 -> 매칭 진행 (환불 없음)
			registerInstructorMatchComplete(event, mailScheduledAt);
		}

		for (ApplicationDeadlineClosedEvent.DesignerMatchInfo designer : event.matchedDesigners()) {
			registerDesignerMatchComplete(event, designer, mailScheduledAt);
		}

	}

	// 어드민 환불 처리 메일 발송
	private void registerAdminRefundRequest(ApplicationDeadlineClosedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			NotificationType.APPLICATION_REFUND_REQUEST_ADMIN,
			Map.of(
				"commissionId", event.commissionId(),
				"commissionTitle", event.commissionTitle(),
				"instructorName", event.instructorName(),
				"instructorEmail", event.instructorEmail(),
				"refundAmount", event.refundAmount(),
				"isCancelled", event.cancelled()
			),
			mailScheduledAt
		));
	}

	// 강사 취소 알림 메일 발송
	private void registerInstructorCancellation(ApplicationDeadlineClosedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.APPLICATION_CANCELLED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}

	// 강사 모집 인원 미달 알림 메일 발송
	private void registerInstructorShortfall(ApplicationDeadlineClosedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.APPLICATION_SHORTFALL_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"requiredCount", event.requiredCount(),
				"designerCount", event.matchedDesignerCount(),
				"shortfallCount", event.requiredCount() - event.matchedDesignerCount(),
				"refundAmount", event.refundAmount()
			),
			mailScheduledAt
		));
	}

	// 강사 매칭 완료 알림 메일 발송
	private void registerInstructorMatchComplete(ApplicationDeadlineClosedEvent event, LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			NotificationType.APPLICATION_MATCHED_INSTRUCTOR,
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"requiredCount", event.requiredCount(),
				"designerCount", event.matchedDesignerCount()
			),
			mailScheduledAt
		));
	}

	// 디자이너 매칭 완료 알림 메일 발송
	private void registerDesignerMatchComplete(
		ApplicationDeadlineClosedEvent event,
		ApplicationDeadlineClosedEvent.DesignerMatchInfo designer,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			designer.email(),
			NotificationType.APPLICATION_MATCHED_DESIGNER,
			Map.of(
				"designerName", designer.name(),
				"commissionTitle", event.commissionTitle(),
				"firstDraftDeadline", event.firstDraftDeadline()
			),
			mailScheduledAt
		));
	}
}

