package ditda.backend.domain.commission.core.processor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.event.CommissionCompletedEvent;
import ditda.backend.domain.commission.core.event.FinalDeadlineClosedEvent;
import ditda.backend.domain.commission.core.event.PayoutRequestedEvent;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDeadlineProcessor {

	private final CommissionRepository commissionRepository;
	private final ApplicationService applicationService;
	private final ApplicationEventPublisher eventPublisher;
	private final CommissionPricePolicy commissionPricePolicy;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void process(Long commissionId, LocalDateTime mailScheduledAt) {

		// 외주 조회
		Commission commission = commissionRepository.findWithInstructorAndAssignedDesignerById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		// 지원자 조회
		List<CommissionApplication> applications = applicationService.getApplicantsWithDesignerAndUser(commissionId);

		applyFinalDeadline(commission, applications, mailScheduledAt);

		publishFinalDeadlineClosedEvent(commission, applications, mailScheduledAt);

		log.info("외주 최종 마감 처리 완료. commissionId={}, cancelled={}",
			commission.getId(), commission.isCancelled());

	}

	private void applyFinalDeadline(
		Commission commission,
		List<CommissionApplication> applications,
		LocalDateTime mailScheduledAt
	) {

		// CASE1: 강사 시안 미선택 -> 외주 취소
		if (commission.getStatus() == CommissionStatus.DRAFT_SELECTING) {
			// 외주 취소
			commission.cancel();

			publishCancellationPayoutEvent(commission, applications, mailScheduledAt);
			publishFinalDeadlineClosedEvent(commission, applications, mailScheduledAt);

			return;
		}

		// CASE2: 수정 진행 중 -> 자동 최종 확정 + 어드민 정산 요청
		commission.complete();

		Designer assigned = commission.getAssignedDesigner();
		assigned.gainCommissionCompletedReward();

		publishFinalCompletedPayoutEvent(commission, mailScheduledAt);
		publishCommissionCompletedEvent(commission, mailScheduledAt);
	}

	private void publishFinalDeadlineClosedEvent(
		Commission commission,
		List<CommissionApplication> applications,
		LocalDateTime mailScheduledAt
	) {
		eventPublisher.publishEvent(new FinalDeadlineClosedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			commission.isCancelled(),
			mailScheduledAt,
			toSubmittedDesignerInfos(applications)
		));
	}

	private List<FinalDeadlineClosedEvent.DesignerInfo> toSubmittedDesignerInfos(
		List<CommissionApplication> applications
	) {
		return applications.stream()
			.filter(CommissionApplication::isDraftSubmitted)
			.map(a -> new FinalDeadlineClosedEvent.DesignerInfo(
				a.getDesigner().getUser().getEmail(),
				a.getDesigner().getUser().getName()))
			.toList();
	}

	// CASE 1: 제출 디자이너들 기본금 정산
	private void publishCancellationPayoutEvent(
		Commission commission,
		List<CommissionApplication> applications,
		LocalDateTime mailScheduledAt
	) {
		List<PayoutRequestedEvent.PayoutInfo> payouts = applications.stream()
			.filter(CommissionApplication::isDraftSubmitted)
			.map(a -> {
				Designer designer = a.getDesigner();
				int amount = commissionPricePolicy.calculateDraftSubmissionReward(
					commission.getCategoryType(), designer.getLevel());
				return new PayoutRequestedEvent.PayoutInfo(
					designer.getId(), designer.getUser().getName(), designer.getLevel(), amount
				);
			})
			.toList();

		if (payouts.isEmpty()) {
			return;
		}

		eventPublisher.publishEvent(new PayoutRequestedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getName(),
			commission.getInstructor().getUser().getEmail(),
			PayoutRequestedEvent.PayoutReason.FINAL_CANCELLED_BY_DEADLINE,
			payouts,
			mailScheduledAt
		));
	}

	// CASE 2: 선택된 디자이너 정산 (기본금 + 인센티브)
	private void publishFinalCompletedPayoutEvent(Commission commission, LocalDateTime mailScheduledAt) {
		Designer assigned = commission.getAssignedDesigner();
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
			PayoutRequestedEvent.PayoutReason.FINAL_COMPLETED_AUTO,
			List.of(payout),
			mailScheduledAt
		));
	}

	// CASE 2: 강사/디자이너 확정 안내
	private void publishCommissionCompletedEvent(Commission commission, LocalDateTime mailScheduledAt) {
		Designer assigned = commission.getAssignedDesigner();
		eventPublisher.publishEvent(new CommissionCompletedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			assigned.getUser().getEmail(),
			assigned.getUser().getName(),
			true,
			mailScheduledAt
		));

	}
}
