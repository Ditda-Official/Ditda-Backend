package ditda.backend.global.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	// 지원 마감 (ApplicationDeadlineClosed)
	APPLICATION_REFUND_REQUEST_ADMIN(
		"[DITDA] 외주 마감에 따른 환불 처리 요망", "email/admin-refund-request"),
	APPLICATION_CANCELLED_INSTRUCTOR(
		"[DITDA] 신청하신 외주가 지원자 부족으로 취소되었습니다.", "email/commission-cancelled"),
	APPLICATION_SHORTFALL_INSTRUCTOR(
		"[DITDA] 신청하신 외주의 모집 인원이 미달되었습니다.", "email/commission-shortfall-instructor"),
	APPLICATION_MATCHED_INSTRUCTOR(
		"[DITDA] 신청하신 외주의 디자이너 매칭이 완료되었습니다.", "email/commission-matched-instructor"),
	APPLICATION_MATCHED_DESIGNER(
		"[DITDA] 지원하신 외주의 1차 시안 대상자로 선정되었습니다.", "email/commission-matched-designer"),

	// 1차 시안 마감 (FirstDraftDeadlineClosed)
	FIRST_DRAFT_REFUND_REQUEST_ADMIN(
		"[DITDA] 1차 시안 미제출에 따른 환불 처리 요망", "email/admin-refund-request"),
	FIRST_DRAFT_ZERO_INSTRUCTOR(
		"[DITDA] 1차 시안 제출이 없어 외주가 취소되었습니다.", "email/first-draft-zero-instructor"),
	FIRST_DRAFT_SHORTFALL_INSTRUCTOR(
		"[DITDA] 일부 디자이너의 1차 시안이 미제출되었습니다.", "email/first-draft-shortfall-instructor"),
	FIRST_DRAFT_MISSED_DESIGNER(
		"[DITDA] 1차 시안 기한 초과로 외주 진행이 종료되었습니다.", "email/first-draft-missed-designer"),

	// 최종 마감 (FinalDeadlineClosed)
	FINAL_CANCELLED_INSTRUCTOR(
		"[DITDA] 시안 선택 기한 초과로 외주가 취소되었습니다.", "email/final-cancelled-instructor"),
	FINAL_CANCELLED_DESIGNER(
		"[DITDA] 강사 미선택으로 외주가 취소되었습니다.", "email/final-cancelled-designer"),

	// 전원 1차 시안 제출 (AllFirstDraftsSubmitted)
	ALL_FIRST_DRAFTS_SUBMITTED_INSTRUCTOR(
		"[DITDA] 모든 1차 시안이 제출되었습니다. 시안을 선택해 주세요.", "email/first-draft-all-submitted-instructor"),

	// 시안 선택 (DraftSelected)
	DRAFT_SELECTED_DESIGNER(
		"[DITDA] 제출하신 1차 시안이 최종 선택되었습니다.", "email/first-draft-selected-designer"),
	DRAFT_REJECTED_DESIGNER(
		"[DITDA] 1차 시안 선정 결과를 안내해 드립니다.", "email/first-draft-rejected-designer"),

	// 외주 최종 확정 (CommissionCompleted)
	COMMISSION_FINALIZED_INSTRUCTOR(
		"[DITDA] 신청하신 외주가 최종 확정되었습니다.", "email/commission-finalized-instructor"),
	COMMISSION_FINALIZED_DESIGNER(
		"[DITDA] 작업하신 외주가 최종 확정되었습니다.", "email/commission-finalized-designer"),

	// 수정 요청/제출 (RevisionRequested / RevisionSubmitted)
	REVISION_REQUESTED_DESIGNER(
		"[DITDA] 시안 수정 요청이 도착했습니다. 확인해주세요.", "email/revision-requested-designer"),
	REVISION_SUBMITTED_INSTRUCTOR(
		"[DITDA] 수정본이 제출되었습니다. 확인해주세요.", "email/revision-submitted-instructor"),

	// 정산 요청 (PayoutRequested)
	PAYOUT_REQUEST_COMPLETED_ADMIN(
		"[DITDA] 외주 최종 확정 - 디자이너 정산 요청", "email/admin-payout-request"),
	PAYOUT_REQUEST_CANCELLED_ADMIN(
		"[DITDA] 외주 취소 - 제출 디자이너 정산 요청", "email/admin-payout-request"),
	PAYOUT_REQUEST_REJECTED_ADMIN(
		"[DITDA] 시안 선택 완료 - 미선택 디자이너 정산 요청", "email/admin-payout-request"),

	// auth
	EMAIL_VERIFICATION(
		"[DITDA] 이메일 인증 코드", "email/verification-code"),
	DESIGNER_SIGNUP_REVIEW_ADMIN(
		"[DITDA] 새 디자이너 가입 검토 요청", "email/designer-signup-notification"),

	// payment
	DEPOSIT_CONFIRM_REQUEST_ADMIN(
		"[DITDA] 외주 입금 확인 요청", "email/deposit-notification");

	private final String subject;
	private final String template;
}
