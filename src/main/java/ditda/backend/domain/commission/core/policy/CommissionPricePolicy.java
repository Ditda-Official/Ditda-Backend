package ditda.backend.domain.commission.core.policy;

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
	public int calculateApplicationShortfallRefund(CategoryType categoryType, int shortfallCount) {
		int level3Reward = calculateDraftSubmissionReward(categoryType, DesignerLevel.LEVEL_3);
		return level3Reward * shortfallCount * 4 / 3;
	}

	private int textbookCoverInnerReward(DesignerLevel level) {
		return switch (level) {
			case LEVEL_1 -> 40_000;
			case LEVEL_2 -> 50_000;
			case LEVEL_3 -> 60_000;
		};
	}
}
