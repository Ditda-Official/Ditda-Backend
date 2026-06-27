package ditda.backend.domain.commission.core.event;

import java.time.LocalDateTime;
import java.util.List;

import ditda.backend.domain.designer.entity.enums.DesignerLevel;

public record PayoutRequestedEvent(
	Long commissionId,
	String commissionTitle,
	String instructorName,
	String instructorEmail,
	PayoutReason reason,
	List<PayoutInfo> payouts,
	LocalDateTime mailScheduledAt
) {
	public enum PayoutReason {
		FINAL_COMPLETED_AUTO,           // 자동 최종 확정
		FINAL_COMPLETED_MANUAL,         // 강사 수동 최종 확정
		FINAL_CANCELLED_BY_DEADLINE,    // 강사 미선택으로 인한 취소
		DRAFT_SELECTION_REJECTED        // 최종 디자이너로 선택되지 않은 디자이너들
	}

	public record PayoutInfo(
		Long designerId,
		String designerName,
		DesignerLevel level,
		int amount
	) {
	}
}
