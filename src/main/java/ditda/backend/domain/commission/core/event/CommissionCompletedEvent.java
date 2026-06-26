package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;

public record CommissionCompletedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	String designerEmail,
	String designerName,
	boolean autoFinalized,
	LocalDateTime mailScheduledAt
) {
}
