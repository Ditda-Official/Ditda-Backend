package ditda.backend.domain.commission.dashboard.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.dashboard.dto.response.DesignerAnnouncementCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.DesignerDraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.DesignerRevisingCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.enums.AnnouncementResult;
import ditda.backend.domain.commission.dashboard.repository.DashboardCommissionRepository;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerAnnouncementView;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerDraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerRevisingView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerDashboardService {

	private final DashboardCommissionRepository dashboardCommissionRepository;
	private final CommissionPricePolicy commissionPricePolicy;

	// 시안 제출 예정 외주 조회
	public DesignerDraftSubmissionCommissionResponse getDraftSubmissions(Long designerId) {

		List<DesignerDraftSubmissionView> views = dashboardCommissionRepository.findDesignerDraftSubmissionViews(
			designerId,
			CommissionStatus.DRAFT_SUBMITTING,
			Set.of(ApplicationStatus.ASSIGNED, ApplicationStatus.DRAFT_SUBMITTED)
		);

		Map<Long, Integer> maxAmountMap = views.stream()
			.collect(Collectors.toMap(
				view -> view.getCommission().getId(),
				this::calculateMaxAmount
			));

		return DesignerDraftSubmissionCommissionResponse.of(views, maxAmountMap);
	}

	// 발표 대기 외주 조회
	public DesignerAnnouncementCommissionResponse getAnnouncements(Long designerId) {

		List<DesignerAnnouncementView> views = dashboardCommissionRepository.findDesignerAnnouncementViews(
			designerId,
			AnnouncementResult.supportedStatuses(),
			AnnouncementResult.statusesOf(AnnouncementResult.SELECTED)
		);

		return DesignerAnnouncementCommissionResponse.of(views);
	}

	// 수정 중인 외주 조회
	public DesignerRevisingCommissionResponse getRevisingCommissions(Long designerId) {

		List<DesignerRevisingView> views = dashboardCommissionRepository.findDesignerRevisingViews(
			designerId,
			CommissionStatus.EDITING
		);

		return DesignerRevisingCommissionResponse.of(views);
	}

	// 최대 수령금액 계산
	private int calculateMaxAmount(DesignerDraftSubmissionView view) {
		return commissionPricePolicy.calculateFinalPayout(
			view.getCommission().getCategoryType(),
			view.getDesignerLevel()
		);
	}
}
