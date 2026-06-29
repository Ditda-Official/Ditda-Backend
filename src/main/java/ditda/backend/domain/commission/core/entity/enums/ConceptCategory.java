package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConceptCategory {
	TEXTURE("질감"),
	LAYOUT("레이아웃"),
	SHAPE("형태"),
	COLOR("색감"),
	MOOD("무드");

	private final String description;
}
