package ditda.backend.domain.commission.core.facade;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.dto.PriceDetail;
import ditda.backend.domain.commission.core.dto.response.CommissionListResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionSummaryResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.service.DesignerCommissionService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import ditda.backend.domain.designer.service.DesignerService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerCommissionFacade {

	private final DesignerCommissionService designerCommissionService;
	private final DesignerService designerService;
	private final ApplicationService applicationService;
	private final CommissionPricePolicy commissionPricePolicy;

	@Transactional(readOnly = true)
	public CommissionListResponse getRecruitingCommissionList(Long designerId, Pageable pageable) {

		Designer designer = designerService.getById(designerId);
		DesignerLevel level = designer.getLevel();
		Page<Commission> commissions = designerCommissionService.getRecruitingCommissions(pageable);

		Map<Long, PriceDetail> priceDetails = commissions.getContent().stream()
			.collect(Collectors.toMap(
				Commission::getId,
				c -> commissionPricePolicy.getPriceDetail(c.getCategoryType(), level)
			));

		return CommissionListResponse.from(commissions, priceDetails);
	}

	// 시안 제출시 외주 기본 정보 조회
	@Transactional(readOnly = true)
	public CommissionSummaryResponse getCommissionSummary(Long designerId, Long commissionId) {

		// 외주 조회
		Commission commission = designerCommissionService.getById(commissionId);

		// 디자이너 지원 조회 + 시안 제출 상태 검증
		CommissionApplication application = applicationService
			.getApplicationByCommissionAndDesigner(commissionId, designerId);
		application.validateDraftSubmittable();

		return CommissionSummaryResponse.from(commission);
	}
}
