package ditda.backend.global.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

	private static final int BATCH_SIZE = 100;

	private final NotificationOutboxRepository outboxRepository;
	private final EmailSender emailSender;

	@Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
	public void dispatchPendingNotifications() {
		LocalDateTime now = LocalDateTime.now();

		List<NotificationOutbox> pendingAlerts = outboxRepository.findPendingScheduled(
			OutboxStatus.PENDING,
			now,
			Limit.of(BATCH_SIZE)
		);

		if (pendingAlerts.isEmpty()) {
			return;
		}

		log.info("아웃박스 알림 발송 시작. 대상 수={}", pendingAlerts.size());
		for (NotificationOutbox outbox : pendingAlerts) {
			try {
				emailSender.send(
					outbox.getRecipientEmail(),
					outbox.getSubject(),
					outbox.getTemplateName(),
					outbox.getTemplateVariables()
				);

				outbox.markSent();

			} catch (Exception e) {
				log.error("아웃박스 메일 발송 실패. outboxId={}", outbox.getId(), e);
				outbox.recordRetry(e.getMessage());

				// TODO: outbox.getStatus() == FAILED시 디스코드 웹훅
			}
			outboxRepository.save(outbox);
		}

	}
}
