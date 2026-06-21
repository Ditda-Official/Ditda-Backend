package ditda.backend.domain.commission.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileKind {
	MATERIAL(5),    // 자료 첨부
	REFERENCE(3);    // 레퍼런스

	// 새 외주 작성시 카테고리별 첨부파일 최대 개수
	private final int maxCount;
}
