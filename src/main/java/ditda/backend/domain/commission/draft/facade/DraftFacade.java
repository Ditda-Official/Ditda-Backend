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
import ditda.backend.domain.commission.core.event.PayoutRequestedEvent;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftSelectResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.service.DraftService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DraftFacade {

	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final InstructorCommissionService instructorCommissionService;
	private final DraftService draftService;
	private final ApplicationService applicationService;
	private final CommissionPricePolicy commissionPricePolicy;
	private final ApplicationEventPublisher eventPublisher;

	// 1차 시안 목록 조회
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {
		return draftService.getFirstRoundDrafts(instructorId, commissionId);
	}

	// 시안 상세 조회
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {
		return draftService.getDraftDetail(instructorId, commissionId, draftId);
	}

	// 1차 시안 선택
	@Transactional
	public DraftSelectResponse selectDraft(Long instructorId, Long commissionId, Long draftId) {

		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		CommissionApplication selected = draftService.getApplicationForSelection(commission, draftId);
		Designer designer = selected.getDesigner();

		LocalDateTime now = LocalDateTime.now();
		instructorCommissionService.selectDesigner(commission, designer, now);

		applicationService.applySelection(commissionId, selected.getId());

		designer.gainDraftSelectedReward();

		return new DraftSelectResponse(
			commissionId,
			draftId,
			selected.getStatus(),
			commission.getMaxRevision(),
			now
		);
	}

	// 최종 시안 확정
	@Transactional
	public void finalizeDraft(Long instructorId, Long commissionId, Long draftId) {

		// 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 최종 확정 가능 단계인지 검증
		commission.validateFinalizable();

		// 가장 최근 시안과 일치하는지 검증
		CommissionDraft latestDraft = draftService.getLatestDraftOfSelectedApplication(commissionId);
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
