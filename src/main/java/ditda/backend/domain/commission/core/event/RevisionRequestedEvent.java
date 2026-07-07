package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;

public record RevisionRequestedEvent(
	Long commissionId,
	String commissionTitle,
	String designerEmail,
	String designerName,
	int currentRevisionCount,
	LocalDateTime mailScheduledAt
) {
}
