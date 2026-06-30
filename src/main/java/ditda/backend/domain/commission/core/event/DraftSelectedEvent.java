package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;
import java.util.List;

public record DraftSelectedEvent(
	Long commissionId,
	String commissionTitle,
	DesignerInfo selectedDesigner,
	List<DesignerInfo> rejectedDesigners,
	LocalDateTime mailScheduledAt
) {
	public record DesignerInfo(String email, String name) {
	}
}
