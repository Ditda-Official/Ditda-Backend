package ditda.backend.domain.commission.core.processor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDeadlineProcessor {

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

		applyApplicationDeadline(commission, applications, mailScheduledAt);

		log.info("외주 지원 마감 처리 완료. commissionId={}, cancelled={}",
			commission.getId(), commission.isCancelled());
	}

	private void applyApplicationDeadline(
		Commission commission,
		List<CommissionApplication> applications,
		LocalDateTime mailScheduledAt
	) {

		int requiredCount = commission.getDesignerCount();
		int applicantCount = applications.size();

		if (applicantCount == 0) {        // CASE 1: 지원자 0명
			handleNoApplicants(commission, mailScheduledAt);
		} else if (applicantCount < requiredCount) {        // CASE 2: 정원 미달
			handleShortfallApplicants(commission, applications, requiredCount - applicantCount, mailScheduledAt);
		} else {        // CASE 3: 정원 충족
			handleFullApplicants(commission, applications, mailScheduledAt);
		}
	}

	// CASE 1: 지원자 0명 -> 외주 취소 + 전액 환불
	private void handleNoApplicants(Commission commission, LocalDateTime mailScheduledAt) {

		// 외주 취소
		commission.cancel();

		// 환불 금액
		int refundAmount = paymentService.requestFullRefund(commission.getId());

		publishEvent(commission, List.of(), refundAmount, mailScheduledAt);
	}

	// CASE 2: 정원 미달 -> 시안 제출 단계 진입 + 미달 인원 환불
	private void handleShortfallApplicants(
		Commission commission,
		List<CommissionApplication> applications,
		int shortfall,
		LocalDateTime mailScheduledAt
	) {

		// 외주 DRAFT_SUBMITTING 처리
		commission.startDraftSubmitting();

		// 지원자 상태 ASSIGNED로 전이
		applicationService.assignAll(applications);

		// 미달 인원 환불
		int refundAmount = commissionPricePolicy.calculateApplicationShortfallRefund(
			commission.getCategoryType(), shortfall);
		paymentService.requestPartialRefund(commission.getId(), refundAmount);

		publishEvent(commission, applications, refundAmount, mailScheduledAt);
	}

	// CASE 3: 정원 충족 -> 시안 제출 단계 진입
	private void handleFullApplicants(
		Commission commission,
		List<CommissionApplication> applications,
		LocalDateTime mailScheduledAt
	) {

		// 외주 DRAFT_SUBMITTING 처리
		commission.startDraftSubmitting();

		// 선정/탈락 분리
		List<CommissionApplication> selected = selectAssignedApplicants(commission, applications);

		Set<Long> selectedIds = selected.stream()
			.map(CommissionApplication::getId)
			.collect(Collectors.toSet());

		List<CommissionApplication> rejected = applications.stream()
			.filter(a -> !selectedIds.contains(a.getId()))
			.toList();

		// 선정자 상태 ASSIGNED로 전이
		applicationService.assignAll(selected);

		// 탈락자 상태 APPLICATION_REJECTED로 전이
		applicationService.markAllApplicationRejected(rejected);

		publishEvent(commission, selected, 0, mailScheduledAt);
	}

	private List<CommissionApplication> selectAssignedApplicants(
		Commission commission,
		List<CommissionApplication> applications
	) {

		// 모집 디자이너 수
		int capacity = commission.getDesignerCount();

		// 지원시간 오름차순 정렬
		List<CommissionApplication> sorted = applications.stream()
			.sorted(Comparator.comparing(CommissionApplication::getCreatedAt))
			.toList();

		List<CommissionApplication> result = new ArrayList<>();
		Set<Long> selectedIds = new HashSet<>();
		Set<DesignerLevel> levelGuaranteed = new HashSet<>();

		// 레벨별 1명 보장
		for (CommissionApplication app : sorted) {
			DesignerLevel level = app.getDesigner().getLevel();
			if (!levelGuaranteed.contains(level)) {
				result.add(app);
				selectedIds.add(app.getId());
				levelGuaranteed.add(level);

				if (result.size() == capacity) {
					break;
				}
			}
		}

		// 남은 슬롯 선착순
		for (CommissionApplication app : sorted) {
			if (result.size() == capacity) {
				break;
			}

			if (!selectedIds.contains(app.getId())) {
				result.add(app);
				selectedIds.add(app.getId());
			}
		}

		return result;
	}

	private void publishEvent(
		Commission commission,
		List<CommissionApplication> applications,
		int refundAmount,
		LocalDateTime mailScheduledAt
	) {

		eventPublisher.publishEvent(new ApplicationDeadlineClosedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			refundAmount,
			commission.isCancelled(),
			commission.getDesignerCount(),
			applications.size(),
			commission.getFirstDraftDeadline(),
			mailScheduledAt,
			toDesignerMatchInfos(applications)
		));
	}

	private List<ApplicationDeadlineClosedEvent.DesignerMatchInfo> toDesignerMatchInfos(
		List<CommissionApplication> applications
	) {
		return applications.stream()
			.map(a -> new ApplicationDeadlineClosedEvent.DesignerMatchInfo(
				a.getDesigner().getUser().getEmail(),
				a.getDesigner().getUser().getName()))
			.toList();
	}
}

