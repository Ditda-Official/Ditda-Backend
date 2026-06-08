package ditda.backend.domain.payment.notification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ditda.backend.domain.payment.event.DepositNotifiedEvent;
import ditda.backend.global.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositNotifier {

	private final DepositMailer depositMailer;
	private final AdminProperties adminProperties;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onDepositNotifier(DepositNotifiedEvent event) {

		try {
			depositMailer.sendAdminNotification(
				adminProperties.getNotificationEmail(),
				event.commissionId(),
				event.commissionTitle(),
				event.instructorName(),
				event.depositorName(),
				event.amount(),
				event.notifiedAt());
		} catch (Exception exception) {
			log.error("Failed to dispatch deposit notification. commissionId={}", event.commissionId(), exception);
			// TODO : 디스코드 웹훅
		}
	}
}
