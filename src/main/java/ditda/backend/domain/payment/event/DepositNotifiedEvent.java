package ditda.backend.domain.payment.event;

import java.time.LocalDateTime;

public record DepositNotifiedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorName,
	String depositorName,
	int amount,
	LocalDateTime notifiedAt,
	LocalDateTime mailScheduledAt
) {
}
