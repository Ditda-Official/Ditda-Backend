package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConceptCategory {
	BRIGHT("밝은"),
	SOFT("부드러운"),
	LUXURIOUS("고급스러운"),
	INTENSE("강렬한"),
	NEAT("단정한");

	private final String description;
}
