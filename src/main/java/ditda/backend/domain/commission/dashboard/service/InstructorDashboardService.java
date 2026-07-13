package ditda.backend.domain.commission.dashboard.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.dashboard.dto.response.InstructorDraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.InstructorMatchingCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.InstructorRevisingCommissionResponse;
import ditda.backend.domain.commission.dashboard.repository.DashboardCommissionRepository;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorDraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorMatchingView;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorRevisingView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorDashboardService {

	private final DashboardCommissionRepository dashboardCommissionRepository;

	// 시안 제출 현황 조회
	public InstructorDraftSubmissionCommissionResponse getDraftSubmissions(Long instructorId) {

		List<InstructorDraftSubmissionView> views = dashboardCommissionRepository.findInstructorDraftSubmissionViews(
			instructorId,
			Set.of(CommissionStatus.DRAFT_SUBMITTING, CommissionStatus.DRAFT_SELECTING),
			ApplicationStatus.DRAFT_SUBMITTED
		);

		LocalDate today = LocalDate.now();

		return InstructorDraftSubmissionCommissionResponse.of(views, today);
	}

	// 매칭 중인 외주 조회
	public InstructorMatchingCommissionResponse getMatchingCommissions(Long instructorId) {

		List<InstructorMatchingView> views = dashboardCommissionRepository.findInstructorMatchingViews(
			instructorId,
			CommissionStatus.RECRUITING,
			ApplicationStatus.PENDING
		);

		return InstructorMatchingCommissionResponse.of(views);
	}

	// 수정 중인 외주 조회
	public InstructorRevisingCommissionResponse getRevisingCommissions(Long instructorId) {

		List<InstructorRevisingView> views = dashboardCommissionRepository.findInstructorRevisingViews(
			instructorId,
			CommissionStatus.EDITING
		);

		return InstructorRevisingCommissionResponse.of(views);
	}
}
