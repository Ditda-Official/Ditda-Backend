package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConceptTag {
	// 밝은
	CUTE(ConceptCategory.BRIGHT, "귀여운"),
	LIGHT(ConceptCategory.BRIGHT, "경쾌한"),
	CLEAR(ConceptCategory.BRIGHT, "맑은"),
	// 부드러운
	NATURAL(ConceptCategory.SOFT, "내츄럴한"),
	SUBTLE(ConceptCategory.SOFT, "은은한"),
	WARM(ConceptCategory.SOFT, "온화한"),
	// 고급스러운
	ELEGANT(ConceptCategory.LUXURIOUS, "우아한"),
	REFINED(ConceptCategory.LUXURIOUS, "고상한"),
	MODERN(ConceptCategory.LUXURIOUS, "모던한"),
	// 강렬한
	VIVID(ConceptCategory.INTENSE, "화려한"),
	DYNAMIC(ConceptCategory.INTENSE, "다이나믹한"),
	// 단정한
	CALM(ConceptCategory.NEAT, "점잖은");

	private final ConceptCategory category;
	private final String description;
}
