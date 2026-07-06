package ditda.backend.domain.commission.application.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.application.dto.SelectionResult;
import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.exception.ApplicationErrorCode;
import ditda.backend.domain.commission.application.policy.CommissionApplicationAssignmentPolicy;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.ApplicationDeadlineClosedEvent;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.lock.DistributedLock;
import ditda.backend.global.lock.LockKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DesignerApplicationFacade {

	private final CommissionService commissionService;
	private final DesignerService designerService;
	private final ApplicationService applicationService;
	private final CommissionApplicationAssignmentPolicy assignmentPolicy;
	private final ApplicationEventPublisher eventPublisher;

	// 외주 지원
	@DistributedLock(key = LockKeys.COMMISSION_MATCHING)
	public void apply(Long designerId, Long commissionId) {

		// 외주/디자이너 조회
		Commission commission = commissionService.getById(commissionId);
		Designer designer = designerService.getById(designerId);

		// 모집 상태/ 지원 마감일 검증
		commission.validateApplicable();
		commission.validateApplicationDeadlineNotPassed(LocalDate.now());

		// 중복 지원 검증
		if (applicationService.existsPendingApplication(commissionId, designerId)) {
			throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_APPLIED);
		}

		// 지원 저장
		applicationService.saveApplication(CommissionApplication.create(commission, designer));

		// 조기 매칭 확정 판정
		tryEarlyMatching(commission);
	}

	// 외주 지원 취소
	@DistributedLock(key = LockKeys.COMMISSION_MATCHING)
	public void cancel(Long designerId, Long commissionId) {

		// 외주 조회
		Commission commission = commissionService.getById(commissionId);

		// 디자이너 지원 조회
		CommissionApplication application =
			applicationService.getApplicationByCommissionAndDesigner(commissionId, designerId);

		// 취소 가능 기간 검증
		commission.validateApplicationDeadlineNotPassed(LocalDate.now());

		// 지원 취소
		application.cancel();
	}

	// 레벨별 1명 + 잉여 슬롯까지 모두 차면 즉시 매칭 확정
	private void tryEarlyMatching(Commission commission) {

		List<CommissionApplication> pendingApplications = applicationService.getPendingApplicantsWithDesigner(
			commission.getId());

		int distinctLevels = (int)pendingApplications.stream()
			.map(application -> application.getDesigner().getLevel())
			.distinct()
			.count();

		if (!commission.isEarlyMatchingReady(distinctLevels, pendingApplications.size())) {
			return;
		}

		// 외주 및 지원자의 필요한 정보들 join fetch
		Commission matchedCommission = commissionService.getWithInstructorAndUserById(commission.getId());
		List<CommissionApplication> pendingWithUser = applicationService.getPendingApplicantsWithDesignerAndUser(
			commission.getId());

		// 레벨별 1명 + 잉여 슬롯까지 모두 차서 즉시 매칭 확정
		matchedCommission.startDraftSubmitting();

		SelectionResult result = assignmentPolicy.select(pendingWithUser, matchedCommission.getDesignerCount());
		applicationService.assignAll(result.selected());
		applicationService.markAllApplicationRejected(result.rejected());

		// 매칭 완료 알림
		publishMatchedEvent(matchedCommission, result.selected());

		log.info("조기 매칭 확정. commissionId={}, selected={}, rejected={}",
			matchedCommission.getId(), result.selected().size(), result.rejected().size());
	}

	// TODO(#94): 결과별 이벤트 분리로 교체
	// 매칭 확정 안내
	private void publishMatchedEvent(Commission commission, List<CommissionApplication> selected) {
		eventPublisher.publishEvent(new ApplicationDeadlineClosedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			0,
			false,
			commission.getDesignerCount(),
			selected.size(),
			commission.getFirstDraftDeadline(),
			LocalDateTime.now(),
			toDesignerMatchInfos(selected)
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
