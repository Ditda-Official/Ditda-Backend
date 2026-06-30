package ditda.backend.domain.commission.core.notification;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.FirstDraftDeadlineClosedEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirstDraftDeadlineClosedNotifier {

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;

	@EventListener
	public void onFirstDraftDeadlineClosed(FirstDraftDeadlineClosedEvent event) {

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		// CASE 1: 시안 제출자 0명 -> 외주 취소 + 전액 환불
		if (event.cancelled()) {
			registerAdminRefundRequest(event, mailScheduledAt, true);
			registerInstructorZeroSubmission(event, mailScheduledAt);

			for (FirstDraftDeadlineClosedEvent.DesignerInfo d : event.missedDesigners()) {
				registerDesignerDraftMissed(event, d, mailScheduledAt);
			}

			return;
		}

		// CASE 2: 일부 미제출 -> 부분 환불 + 강사 선택 안내
		registerAdminRefundRequest(event, mailScheduledAt, false);
		registerInstructorShortfallSubmission(event, mailScheduledAt);

		for (FirstDraftDeadlineClosedEvent.DesignerInfo d : event.missedDesigners()) {
			registerDesignerDraftMissed(event, d, mailScheduledAt);
		}
	}

	// 어드민 환불 처리 메일 발송
	private void registerAdminRefundRequest(
		FirstDraftDeadlineClosedEvent event, LocalDateTime mailScheduledAt, boolean isCancelled
	) {
		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			"[DITDA] 1차 시안 미제출에 따른 환불 처리 요망",
			"email/admin-refund-request",
			Map.of(
				"commissionId", event.commissionId(),
				"commissionTitle", event.commissionTitle(),
				"instructorName", event.instructorName(),
				"instructorEmail", event.instructorEmail(),
				"refundAmount", event.refundAmount(),
				"isCancelled", isCancelled
			),
			mailScheduledAt
		));
	}

	// 강사 취소 알림 메일 발송
	private void registerInstructorZeroSubmission(
		FirstDraftDeadlineClosedEvent event, LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			"[DITDA] 1차 시안 제출이 없어 외주가 취소되었습니다.",
			"email/first-draft-zero-instructor",
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"refundAmount", event.refundAmount()
			),
			mailScheduledAt
		));
	}

	// 강사 시안 미제출자 존재 알림 메일 발송
	private void registerInstructorShortfallSubmission(
		FirstDraftDeadlineClosedEvent event, LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			event.instructorEmail(),
			"[DITDA] 일부 디자이너의 1차 시안이 미제출되었습니다.",
			"email/first-draft-shortfall-instructor",
			Map.of(
				"instructorName", event.instructorName(),
				"commissionTitle", event.commissionTitle(),
				"submittedCount", event.submittedDesigners().size(),
				"missedCount", event.missedDesigners().size(),
				"refundAmount", event.refundAmount(),
				"finalDeadline", event.finalDeadline()
			),
			mailScheduledAt
		));
	}

	// 디자이너 1차 시안 선택 기한 초과 메일 발송
	private void registerDesignerDraftMissed(
		FirstDraftDeadlineClosedEvent event,
		FirstDraftDeadlineClosedEvent.DesignerInfo designer,
		LocalDateTime mailScheduledAt
	) {
		outboxRepository.save(NotificationOutbox.create(
			designer.email(),
			"[DITDA] 1차 시안 기한 초과로 외주 진행이 종료되었습니다.",
			"email/first-draft-missed-designer",
			Map.of(
				"designerName", designer.name(),
				"commissionTitle", event.commissionTitle()
			),
			mailScheduledAt
		));
	}
}
