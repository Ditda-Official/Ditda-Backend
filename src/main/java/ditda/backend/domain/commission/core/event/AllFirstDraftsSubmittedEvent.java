package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;

public record AllFirstDraftsSubmittedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	int submittedCount,
	LocalDateTime mailScheduledAt
) {
}
