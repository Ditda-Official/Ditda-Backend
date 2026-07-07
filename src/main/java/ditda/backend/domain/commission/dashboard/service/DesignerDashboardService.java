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
import ditda.backend.domain.commission.dashboard.dto.response.DesignerDraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.repository.DashboardCommissionRepository;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerDraftSubmissionView;
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

	// 최대 수령금액 계산
	private int calculateMaxAmount(DesignerDraftSubmissionView view) {
		return commissionPricePolicy.calculateFinalPayout(
			view.getCommission().getCategoryType(),
			view.getDesignerLevel()
		);
	}
}
