package ditda.backend.domain.auth.event;

import java.util.List;

public record DesignerSignedUpEvent(
	Long userId,
	String name,
	String email,
	List<String> portfolioKeys
) {
}
