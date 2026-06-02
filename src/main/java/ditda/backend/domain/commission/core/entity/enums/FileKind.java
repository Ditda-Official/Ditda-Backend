package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileKind {
	MATERIAL(3),    // 자료 첨부
	REFERENCE(3);    // 레퍼런스

	private final int maxCount;
}
