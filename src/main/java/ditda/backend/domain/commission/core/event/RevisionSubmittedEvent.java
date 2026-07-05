package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;

public record RevisionSubmittedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	int currentRevisionCount,
	LocalDateTime mailScheduledAt
) {
}
