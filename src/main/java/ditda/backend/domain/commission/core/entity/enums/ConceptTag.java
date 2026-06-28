package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConceptTag {

	// 질감
	DIMENSIONAL(ConceptCategory.TEXTURE, "입체감 있는"),
	FLAT(ConceptCategory.TEXTURE, "평면적인"),
	ROUGH(ConceptCategory.TEXTURE, "거친"),
	SMOOTH(ConceptCategory.TEXTURE, "매끈한"),

	// 레이아웃
	ORDERLY(ConceptCategory.LAYOUT, "정돈된"),
	DYNAMIC(ConceptCategory.LAYOUT, "역동적인"),
	SPACIOUS(ConceptCategory.LAYOUT, "여백이 많은"),
	DENSE(ConceptCategory.LAYOUT, "꽉 찬"),

	// 형태
	ROUND(ConceptCategory.SHAPE, "둥근"),
	ANGULAR(ConceptCategory.SHAPE, "각진"),
	FREEFORM(ConceptCategory.SHAPE, "자유로운"),
	GEOMETRIC(ConceptCategory.SHAPE, "기하학적인"),

	// 색감
	VIVID(ConceptCategory.COLOR, "화려한"),
	MUTED(ConceptCategory.COLOR, "차분한"),
	BRIGHT(ConceptCategory.COLOR, "밝은"),
	DARK(ConceptCategory.COLOR, "어두운"),

	// 무드
	CUTE(ConceptCategory.MOOD, "귀여운"),
	CHIC(ConceptCategory.MOOD, "시크한"),
	EMOTIONAL(ConceptCategory.MOOD, "감성적인"),
	PROFESSIONAL(ConceptCategory.MOOD, "전문적인");

	private final ConceptCategory category;
	private final String description;
}
