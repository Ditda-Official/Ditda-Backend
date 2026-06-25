package ditda.backend.domain.commission.core.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.ApplicationDeadlineClosedEvent;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionDeadlineProcessor {

	private final CommissionRepository commissionRepository;
	private final ApplicationService applicationService;
	private final CommissionPricePolicy commissionPricePolicy;
	private final PaymentService paymentService;
	private final ApplicationEventPublisher eventPublisher;

	// ============ 지원 마감 ============
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processApplicationDeadline(Long commissionId, LocalDateTime mailScheduledAt) {

		// 외주 조회
		Commission commission = commissionRepository.findWithInstructorAndUserById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		// 지원자 조회
		List<CommissionApplication> applications = applicationService.getApplicantsWithDesignerAndUser(commissionId);

		// 모집 정원 및 지원자 수
		int requiredCount = commission.getDesignerCount();
		int applicantCount = applications.size();

		// 환불 금액
		int refundAmount = 0;

		// 취소 여부
		boolean cancelled = false;

		if (applicantCount == 0) {        // 지원자 0명 -> 취소 및 전액 환불
			// 외주 CANCELLED 처리
			commission.cancel();
			cancelled = true;

			// 전액 환불
			refundAmount = paymentService.requestFullRefund(commissionId);

		} else if (applicantCount < requiredCount) {    // 지원자 < 모집 인원 -> 매칭 진행 및 미달 인원 환불

			// 외주 IN_PROGRESS 처리
			commission.startProgress();

			// 지원자 상태를 ASSIGNED로 변경
			applicationService.assignAll(applications);

			// 미달 인원 환불
			refundAmount = commissionPricePolicy.calculateApplicationShortfallRefund(
				commission.getCategoryType(),
				requiredCount - applicantCount
			);

			paymentService.requestPartialRefund(commissionId, refundAmount);
		} else {        // 인원 충족 -> 매칭 진행

			// 외주 IN_PROGRESS 처리
			commission.startProgress();

			// 지원자 상태를 ASSIGNED로 변경
			applicationService.assignAll(applications);
		}

		eventPublisher.publishEvent(new ApplicationDeadlineClosedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			refundAmount,
			cancelled,
			requiredCount,
			applicantCount,
			commission.getFirstDraftDeadline(),
			mailScheduledAt,
			applications.stream()
				.map(app -> new ApplicationDeadlineClosedEvent.DesignerMatchInfo(
					app.getDesigner().getUser().getEmail(),
					app.getDesigner().getUser().getName()))
				.toList()
		));

		log.info("외주 마감 처리 완료. commissionId={}, cancelled={},  지원자 수={}, 환불금액={}",
			commission.getId(), cancelled, applicantCount, refundAmount);

	}

	// ============ 1차 시안 마감 ============

	// ============ 최종 마감 ============

}
