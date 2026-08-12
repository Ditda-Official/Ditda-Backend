package ditda.backend.global.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CorrelationData;
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
	private final MailPublisher mailPublisher;

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

		int sent = 0;
		int failed = 0;

		for (NotificationOutbox outbox : pendingAlerts) {
			try {
				MailMessage message = new MailMessage(
					outbox.getType().name(),
					outbox.getRecipientEmail(),
					outbox.getTemplateVariables()
				);

				CorrelationData correlationData = new CorrelationData(outbox.getId().toString());
				mailPublisher.publish(message, correlationData);

				CorrelationData.Confirm confirm = correlationData.getFuture().get(5, TimeUnit.SECONDS);
				if (confirm.ack() && correlationData.getReturned() == null) {
					outbox.markSent();
					sent++;
				} else {
					String errorMessage = !confirm.ack() ? "Broker Not Confirmed" : "Message Returned (Unroutable)";
					outbox.recordRetry(errorMessage);
					failed++;

					log.warn("Notification was not delivered. outboxId={}, type={}, retryCount={}, reason={}",
						outbox.getId(), outbox.getType(), outbox.getRetryCount(), errorMessage);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("Interrupted while publishing notifications. outboxId={}", outbox.getId(), e);
				break;
			} catch (Exception e) {
				outbox.recordRetry(e.getClass().getSimpleName());
				failed++;

				log.warn("Notification publish failed. outboxId={}, type={}, retryCount={}",
					outbox.getId(), outbox.getType(), outbox.getRetryCount(), e);
			}

			outboxRepository.save(outbox);

			if (outbox.getStatus() == OutboxStatus.FAILED) {
				log.error("Notification permanently failed. outboxId={}, type={}, retryCount={}",
					outbox.getId(), outbox.getType(), outbox.getRetryCount());
				// TODO: outbox.getStatus() == FAILED시 디스코드 웹훅
			}
		}

		log.info("Notification outbox batch finished. total={}, sent={}, failed={}",
			pendingAlerts.size(), sent, failed);
	}
}
