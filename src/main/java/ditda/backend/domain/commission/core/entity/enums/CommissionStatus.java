package ditda.backend.domain.commission.core.entity.enums;

public enum CommissionStatus {
	PENDING,		// 결제 대기
	RECRUITING,		// 디자이너 모집/지원
	IN_PROGRESS,	// 1차 시안 제출 대기 / 강사 선택 대기
	EDITING,		// 수정 진행 중
	COMPLETED,		// 최종 확정
	CANCELLED		// 취소
}
