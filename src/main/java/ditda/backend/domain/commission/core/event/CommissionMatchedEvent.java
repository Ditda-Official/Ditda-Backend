package ditda.backend.domain.commission.core.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CommissionMatchedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorEmail,
	String instructorName,
	int requiredCount,
	int matchedDesignerCount,
	LocalDate firstDraftDeadline,
	LocalDateTime mailScheduledAt,
	List<DesignerMatchInfo> matchedDesigners
) {

	public record DesignerMatchInfo(String email, String name) {
	}
}
