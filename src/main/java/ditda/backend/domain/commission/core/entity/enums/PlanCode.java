package ditda.backend.domain.commission.core.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanCode {

	BASIC(3, 400000, 3, "디자이너 3명의 시안을 받아볼 수 있습니다."),
	PLUS(4, 480000, 3, "디자이너 4명의 시안을 받아볼 수 있습니다."),
	MAX(5, 560000, 3, "디자이너 5명의 시안을 받아볼 수 있습니다.");

	private final int designerCount;
	private final int price;
	private final int baseRevision;
	private final String description;
}
