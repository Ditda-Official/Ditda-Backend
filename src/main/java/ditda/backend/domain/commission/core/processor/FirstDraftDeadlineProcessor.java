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
import ditda.backend.domain.commission.core.event.FirstDraftDeadlineClosedEvent;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstDraftDeadlineProcessor {

	private final CommissionRepository commissionRepository;
	private final ApplicationService applicationService;
	private final CommissionPricePolicy commissionPricePolicy;
	private final PaymentService paymentService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void process(Long commissionId, LocalDateTime mailScheduledAt) {

		// 외주 조회
		Commission commission = commissionRepository.findWithInstructorAndUserById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		// 지원자 조회
		List<CommissionApplication> applications = applicationService.getApplicantsWithDesignerAndUser(commissionId);

		// 1차 시안 제출자/미제출자
		List<CommissionApplication> submitted = applications.stream()
			.filter(CommissionApplication::isDraftSubmitted)
			.toList();
		List<CommissionApplication> missed = applications.stream()
			.filter(CommissionApplication::isAssigned)
			.toList();

		// 환불 금액
		int refundAmount = applyFirstDraftDeadline(commission, submitted, missed);

		publishEvent(commission, submitted, missed, refundAmount, mailScheduledAt);

		log.info("외주 1차 시안 마감 처리 완료. commissionId={}, cancelled={}, 환불금액={}",
			commission.getId(), commission.isCancelled(), refundAmount);
	}

	private int applyFirstDraftDeadline(
		Commission commission,
		List<CommissionApplication> submitted,
		List<CommissionApplication> missed
	) {

		// CASE1: 시안 제출자 0명
		if (submitted.isEmpty()) {
			return handleZeroSubmission(commission, missed);
		}

		// CASE2: 일부 미제출
		return handlePartialSubmission(commission, missed);

	}

	// CASE 1: 시안 제출자 0명 -> 외주 취소 + 전액 환불
	private int handleZeroSubmission(Commission commission, List<CommissionApplication> missed) {

		// 외주 취소
		commission.cancel();

		// 시안 미제출자 DRAFT_MISSED 처리
		applicationService.markAllDraftMissed(missed);

		// 전액 환불
		return paymentService.requestFullRefund(commission.getId());
	}

	// CASE 2: 일부 미제출 -> 강사 선택 단계 진입 + 부분 환불
	private int handlePartialSubmission(Commission commission, List<CommissionApplication> missed) {

		// 외주 DRAFT_SELECTING 처리
		commission.startDraftSelecting();

		// 시안 미제출자 DRAFT_MISSED 처리
		applicationService.markAllDraftMissed(missed);

		// 부분 환불
		List<DesignerLevel> missedLevels = missed.stream()
			.map(a -> a.getDesigner().getLevel())
			.toList();

		int refundAmount = commissionPricePolicy.calculateFirstDraftMissedRefund(
			commission.getCategoryType(), missedLevels);

		paymentService.requestPartialRefund(commission.getId(), refundAmount);

		return refundAmount;
	}

	private void publishEvent(
		Commission commission,
		List<CommissionApplication> submitted,
		List<CommissionApplication> missed,
		int refundAmount,
		LocalDateTime mailScheduledAt
	) {
		eventPublisher.publishEvent(new FirstDraftDeadlineClosedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			refundAmount,
			commission.isCancelled(),
			commission.getFinalDeadline(),
			mailScheduledAt,
			toDesignerInfos(submitted),
			toDesignerInfos(missed)
		));
	}

	private List<FirstDraftDeadlineClosedEvent.DesignerInfo> toDesignerInfos(List<CommissionApplication> applications) {
		return applications.stream()
			.map(a -> new FirstDraftDeadlineClosedEvent.DesignerInfo(
				a.getDesigner().getUser().getEmail(),
				a.getDesigner().getUser().getName()
			))
			.toList();
	}
}
