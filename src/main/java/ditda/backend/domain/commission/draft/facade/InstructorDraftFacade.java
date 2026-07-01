package ditda.backend.domain.commission.draft.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.CommissionCompletedEvent;
import ditda.backend.domain.commission.core.event.DraftSelectedEvent;
import ditda.backend.domain.commission.core.event.PayoutRequestedEvent;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftSelectResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.service.InstructorDraftService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorDraftFacade {

	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final InstructorCommissionService instructorCommissionService;
	private final InstructorDraftService instructorDraftService;
	private final ApplicationService applicationService;
	private final CommissionPricePolicy commissionPricePolicy;
	private final ApplicationEventPublisher eventPublisher;

	// 1차 시안 목록 조회
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {
		return instructorDraftService.getFirstRoundDrafts(instructorId, commissionId);
	}

	// 시안 상세 조회
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {
		return instructorDraftService.getDraftDetail(instructorId, commissionId, draftId);
	}

	// 1차 시안 선택
	@Transactional
	public DraftSelectResponse selectDraft(Long instructorId, Long commissionId, Long draftId) {

		// 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 지원자 조회
		List<CommissionApplication> applications = applicationService.getApplicantsWithDesignerAndUser(commissionId);

		// 해당 시안의 디자이너 조회
		CommissionApplication selected = instructorDraftService.getApplicationForSelection(commission, draftId);

		// 미선택 디자이너
		List<CommissionApplication> rejected = filterRejected(applications, selected);

		// 디자이너 선정 + 도메인 상태 변경
		LocalDateTime now = LocalDateTime.now();
		applyDesignerSelection(commission, applications, selected, now);

		// 이벤트 발행
		LocalDateTime mailScheduledAt = LocalDateTime.now(ZONE_KST);
		publishRejectedPayoutEvent(commission, rejected, mailScheduledAt);
		publishDraftSelectedEvent(commission, selected, rejected, mailScheduledAt);

		return new DraftSelectResponse(
			commissionId,
			draftId,
			selected.getStatus(),
			commission.getMaxRevision(),
			now
		);
	}

	// 미선택 디자이너 추출
	private List<CommissionApplication> filterRejected(
		List<CommissionApplication> applications,
		CommissionApplication selected
	) {

		return applications.stream()
			.filter(a -> a.isDraftSubmitted() && !a.getId().equals(selected.getId()))
			.toList();
	}

	private void applyDesignerSelection(
		Commission commission,
		List<CommissionApplication> applications,
		CommissionApplication selected,
		LocalDateTime now
	) {
		Designer designer = selected.getDesigner();

		// 디자이너 선택
		instructorCommissionService.selectDesigner(commission, designer, now);

		// 지원 상태 변경
		applicationService.applySelection(applications, selected.getId());

		// 선택된 디자이너 경험치 증가
		designer.gainDraftSelectedReward();
	}

	// 어드민: 미선택 디자이너 기본금 정산 요청
	private void publishRejectedPayoutEvent(
		Commission commission,
		List<CommissionApplication> rejected,
		LocalDateTime mailScheduledAt
	) {
		if (rejected.isEmpty()) {
			return;
		}

		List<PayoutRequestedEvent.PayoutInfo> payouts = rejected.stream()
			.map(a -> {
				Designer designer = a.getDesigner();
				int amount = commissionPricePolicy.calculateDraftSubmissionReward(
					commission.getCategoryType(), designer.getLevel());
				return new PayoutRequestedEvent.PayoutInfo(
					designer.getId(), designer.getUser().getName(), designer.getLevel(), amount
				);
			})
			.toList();

		eventPublisher.publishEvent(new PayoutRequestedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getName(),
			commission.getInstructor().getUser().getEmail(),
			PayoutRequestedEvent.PayoutReason.DRAFT_SELECTION_REJECTED,
			payouts,
			mailScheduledAt
		));
	}

	// 디자이너: 선택/미선택 안내
	private void publishDraftSelectedEvent(
		Commission commission,
		CommissionApplication selected,
		List<CommissionApplication> rejected,
		LocalDateTime mailScheduledAt
	) {
		DraftSelectedEvent.DesignerInfo selectedInfo = new DraftSelectedEvent.DesignerInfo(
			selected.getDesigner().getUser().getEmail(),
			selected.getDesigner().getUser().getName()
		);

		List<DraftSelectedEvent.DesignerInfo> rejectedInfos = rejected.stream()
			.map(a -> new DraftSelectedEvent.DesignerInfo(
				a.getDesigner().getUser().getEmail(),
				a.getDesigner().getUser().getName()))
			.toList();

		eventPublisher.publishEvent(new DraftSelectedEvent(
			commission.getId(),
			commission.getTitle(),
			selectedInfo,
			rejectedInfos,
			mailScheduledAt
		));

	}

	// 최종 시안 확정
	@Transactional
	public void finalizeDraft(Long instructorId, Long commissionId, Long draftId) {

		// 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 가장 최근 시안과 일치하는지 검증
		CommissionDraft latestDraft = instructorDraftService.getLatestDraftOfSelectedApplication(commissionId);
		if (!latestDraft.getId().equals(draftId)) {
			throw new GeneralException(DraftErrorCode.DRAFT_NOT_LATEST);
		}

		// 외주 최종 확정 처리
		commission.complete();

		// 어드민 정산 요청 + 강사/디자이너 외주 확정 안내 이벤트 발생
		Designer assigned = latestDraft.getCommissionApplication().getDesigner();
		assigned.gainCommissionCompletedReward();

		LocalDateTime mailScheduledAt = LocalDateTime.now(ZONE_KST);

		publishPayoutRequestedEvent(commission, assigned, mailScheduledAt);
		publishCommissionCompletedEvent(commission, assigned, mailScheduledAt);

	}

	// 어드민: 확정된 디자이너 정산 요청
	private void publishPayoutRequestedEvent(Commission commission, Designer assigned, LocalDateTime mailScheduledAt) {
		int amount = commissionPricePolicy.calculateFinalPayout(
			commission.getCategoryType(), assigned.getLevel());

		PayoutRequestedEvent.PayoutInfo payout = new PayoutRequestedEvent.PayoutInfo(
			assigned.getId(), assigned.getUser().getName(), assigned.getLevel(), amount
		);

		eventPublisher.publishEvent(new PayoutRequestedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getName(),
			commission.getInstructor().getUser().getEmail(),
			PayoutRequestedEvent.PayoutReason.FINAL_COMPLETED_MANUAL,
			List.of(payout),
			mailScheduledAt
		));
	}

	// 디자이너: 외주 확정 안내
	private void publishCommissionCompletedEvent(Commission commission, Designer assigned,
		LocalDateTime mailScheduledAt) {
		eventPublisher.publishEvent(new CommissionCompletedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			assigned.getUser().getEmail(),
			assigned.getUser().getName(),
			false,
			mailScheduledAt
		));
	}
}
