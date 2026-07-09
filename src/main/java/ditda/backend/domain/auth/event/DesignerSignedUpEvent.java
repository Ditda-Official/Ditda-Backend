package ditda.backend.domain.auth.event;

import java.time.LocalDateTime;

public record DesignerSignedUpEvent(
	Long designerId,
	String name,
	String email,
	boolean hasPortfolio,
	LocalDateTime mailScheduledAt
) {
}
