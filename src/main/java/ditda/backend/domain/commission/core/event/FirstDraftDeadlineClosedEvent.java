package ditda.backend.domain.commission.core.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FirstDraftDeadlineClosedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	int refundAmount,
	boolean cancelled,
	LocalDate finalDeadline,
	LocalDateTime mailScheduledAt,
	List<DesignerInfo> submittedDesigners,
	List<DesignerInfo> missedDesigners
) {
	public record DesignerInfo(String email, String name) {
	}
}
