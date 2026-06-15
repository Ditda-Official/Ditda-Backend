package ditda.backend.domain.designer.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DesignerLevel {

	LEVEL_1(500),    // LEVEL_2 까지 500
	LEVEL_2(1000),   // LEVEL_3 까지 1000
	LEVEL_3(0);      // 만렙

	private final int requiredExp;

	public boolean isMax() {
		return this == LEVEL_3;
	}

	public boolean canLevelUp(int exp) {
		return !isMax() && exp >= requiredExp;
	}

	public DesignerLevel next() {
		return switch (this) {
			case LEVEL_1 -> LEVEL_2;
			case LEVEL_2 -> LEVEL_3;
			case LEVEL_3 -> LEVEL_3;
		};
	}
}
