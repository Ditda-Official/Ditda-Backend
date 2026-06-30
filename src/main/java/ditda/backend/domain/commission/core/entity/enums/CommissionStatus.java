package ditda.backend.domain.commission.core.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum CommissionStatus {
	PENDING,        // 결제 대기
	RECRUITING,        // 디자이너 모집/지원
	DRAFT_SUBMITTING,    // 1차 시안 제출 대기
	DRAFT_SELECTING,    // 강사 선택 대기
	EDITING,        // 수정 진행 중
	COMPLETED,        // 최종 확정
	CANCELLED;        // 취소

	// 진행 중인 외주 상태
	private static final Set<CommissionStatus> ONGOING =
		EnumSet.of(RECRUITING, DRAFT_SUBMITTING, DRAFT_SELECTING, EDITING);

	public static Set<CommissionStatus> ongoingStatuses() {
		return Collections.unmodifiableSet(ONGOING);
	}
}
