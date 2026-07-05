package ditda.backend.domain.commission.application.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.application.dto.SelectionResult;
import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.exception.ApplicationErrorCode;
import ditda.backend.domain.commission.application.policy.CommissionApplicationAssignmentPolicy;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.lock.DistributedLock;
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

	// 외주 지원
	@DistributedLock(key = "'commission:matching:' + #commissionId")
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

		// 레벨별 1명 + 잉여 슬롯까지 모두 차서 즉시 매칭 확정
		commission.startDraftSubmitting();

		SelectionResult result = assignmentPolicy.select(pendingApplications, commission.getDesignerCount());
		applicationService.assignAll(result.selected());
		applicationService.markAllApplicationRejected(result.rejected());

		log.info("조기 매칭 확정. commissionId={}, selected={}, rejected={}",
			commission.getId(), result.selected().size(), result.rejected().size());
	}
}
