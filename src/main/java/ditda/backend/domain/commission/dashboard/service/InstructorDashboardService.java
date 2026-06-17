package ditda.backend.domain.commission.dashboard.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.CommissionApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.dashboard.dto.response.DraftSubmissionCommissionResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorDashboardService {

	private final InstructorCommissionService commissionService;
	private final CommissionApplicationService commissionApplicationService;

	// 시안 제출 현황 조회
	public DraftSubmissionCommissionResponse getDraftSubmissions(Long instructorId) {

		// 시안 제출 중인 외주 조회
		List<Commission> commissions = commissionService.getCommissionByInstructorAndStatus(
			instructorId,
			CommissionStatus.IN_PROGRESS
		);

		// 외주 Id 추출
		List<Long> commissionIds = commissions.stream().map(Commission::getId).toList();

		// 시안을 제출한 디자이너 수 조회
		Map<Long, Long> draftSubmissionCount = commissionApplicationService.countApplicationByStatus(
			commissionIds,
			ApplicationStatus.DRAFT_SUBMITTED
		);

		return DraftSubmissionCommissionResponse.of(commissions, draftSubmissionCount, LocalDate.now());
	}
}
