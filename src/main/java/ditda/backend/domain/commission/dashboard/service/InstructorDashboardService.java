package ditda.backend.domain.commission.dashboard.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.CommissionApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.dashboard.dto.response.DraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.MatchingCommissionResponse;
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

	// 매칭 중인 외주 조회
	public MatchingCommissionResponse getMatchingCommissions(Long instructorId) {

		// 모집 중인 외주 조회
		List<Commission> commissions = commissionService.getCommissionByInstructorAndStatus(
			instructorId,
			CommissionStatus.RECRUITING
		);

		// 외주 Id 추출
		List<Long> commissionIds = commissions.stream().map(Commission::getId).toList();

		// 외주별 지원자가 존재하는 레벨 종류 수
		Map<Long, Long> distinctLevelCount = commissionApplicationService.countDistinctLevelByStatus(
			commissionIds,
			ApplicationStatus.PENDING
		);

		// 외주별 전체 지원자 수
		Map<Long, Long> applicantCount = commissionApplicationService.countApplicationByStatus(
			commissionIds,
			ApplicationStatus.PENDING
		);

		// 외주별 매칭 확정 인원 계산
		Map<Long, Long> matchedCount = commissions.stream()
			.collect(Collectors.toMap(
				Commission::getId,
				c -> (long)c.matchedCount(
					distinctLevelCount.get(c.getId()).intValue(),
					applicantCount.get(c.getId()).intValue())
			));

		return MatchingCommissionResponse.of(commissions, matchedCount);
	}
}
