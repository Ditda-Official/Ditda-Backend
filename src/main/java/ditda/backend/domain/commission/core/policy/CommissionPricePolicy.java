package ditda.backend.domain.commission.core.policy;

import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;

@Component
public class CommissionPricePolicy {

	// 디자이너 기본금
	public int calculateDraftSubmissionReward(CategoryType category, DesignerLevel level) {
		return switch (category) {
			case FLYER_TEXTBOOK_COVER_INNER -> textbookCoverInnerReward(level);
		};
	}

	// 최종 확정시 인센티브
	public int calculateFinalSelectionIncentive(CategoryType category) {
		return switch (category) {
			case FLYER_TEXTBOOK_COVER_INNER -> 150_000;
		};
	}

	// 지원 마감 정원 미달 환불 금액
	public int calculateApplicationShortfallRefund(CategoryType category, int shortfallCount) {
		int level3Reward = calculateDraftSubmissionReward(category, DesignerLevel.LEVEL_3);
		return level3Reward * shortfallCount * 4 / 3;
	}

	// 1차 시안 미제출 디자이너 기본금 합산 환불 금액
	public int calculateFirstDraftMissedRefund(CategoryType category, List<DesignerLevel> missedLevels) {
		return missedLevels.stream()
			.mapToInt(level -> calculateDraftSubmissionReward(category, level))
			.sum();
	}

	// 최종 확정시 선택된 디자이너 정산 금액
	public int calculateFinalPayout(CategoryType category, DesignerLevel level) {
		return calculateDraftSubmissionReward(category, level) + calculateFinalSelectionIncentive(category);
	}

	private int textbookCoverInnerReward(DesignerLevel level) {
		return switch (level) {
			case LEVEL_1 -> 40_000;
			case LEVEL_2 -> 50_000;
			case LEVEL_3 -> 60_000;
		};
	}
}
