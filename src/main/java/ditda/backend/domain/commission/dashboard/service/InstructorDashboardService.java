package ditda.backend.domain.commission.dashboard.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.dashboard.dto.response.DraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.MatchingCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.RevisingCommissionResponse;
import ditda.backend.domain.commission.dashboard.repository.DashboardCommissionRepository;
import ditda.backend.domain.commission.dashboard.repository.projection.DraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.MatchingView;
import ditda.backend.domain.commission.dashboard.repository.projection.RevisingView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorDashboardService {

	private final DashboardCommissionRepository dashboardCommissionRepository;

	// 시안 제출 현황 조회
	public DraftSubmissionCommissionResponse getDraftSubmissions(Long instructorId) {

		List<DraftSubmissionView> views = dashboardCommissionRepository.findDraftSubmissionViews(
			instructorId,
			CommissionStatus.DRAFT_SUBMITTING,
			ApplicationStatus.DRAFT_SUBMITTED
		);

		LocalDate today = LocalDate.now();

		return DraftSubmissionCommissionResponse.of(views, today);
	}

	// 매칭 중인 외주 조회
	public MatchingCommissionResponse getMatchingCommissions(Long instructorId) {

		List<MatchingView> views = dashboardCommissionRepository.findMatchingViews(
			instructorId,
			CommissionStatus.RECRUITING,
			ApplicationStatus.PENDING
		);

		return MatchingCommissionResponse.of(views);
	}

	// 수정 중인 외주 조회
	public RevisingCommissionResponse getRevisingCommissions(Long instructorId) {

		List<RevisingView> views = dashboardCommissionRepository.findRevisingViews(
			instructorId,
			CommissionStatus.EDITING
		);

		return RevisingCommissionResponse.of(views);
	}
}
