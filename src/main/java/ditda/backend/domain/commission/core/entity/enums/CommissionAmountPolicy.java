package ditda.backend.domain.commission.core.entity.enums;

import java.util.Arrays;

import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import ditda.backend.domain.designer.exception.DesignerErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommissionAmountPolicy {

	LEVEL_1(DesignerLevel.LEVEL_1, 40_000),
	LEVEL_2(DesignerLevel.LEVEL_2, 50_000),
	LEVEL_3(DesignerLevel.LEVEL_3, 60_000);

	private static final int MAX_AMOUNT_BONUS = 150_000;

	private final DesignerLevel level;
	private final int baseAmount;

	public int getMaxAmount() {
		return baseAmount + MAX_AMOUNT_BONUS;
	}

	public static CommissionAmountPolicy from(DesignerLevel level) {
		return Arrays.stream(values())
			.filter(policy -> policy.level == level)
			.findFirst()
			.orElseThrow(() -> new GeneralException(DesignerErrorCode.DESIGNER_LEVEL_POLICY_NOT_FOUND));
	}
}
