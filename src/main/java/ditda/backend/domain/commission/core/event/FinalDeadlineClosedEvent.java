package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;
import java.util.List;

public record FinalDeadlineClosedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	boolean cancelled,
	LocalDateTime mailScheduledAt,
	List<DesignerInfo> submittedDesigners
) {
	public record DesignerInfo(String email, String name) {
	}
}
