package ditda.backend.domain.commission.core.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.response.CommissionListResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.mapper.CommissionMapper;
import ditda.backend.domain.commission.core.service.DesignerCommissionService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerCommissionFacade {

	private final DesignerCommissionService designerCommissionService;
	private final DesignerService designerService;
	private final CommissionMapper commissionMapper;

	@Transactional(readOnly = true)
	public CommissionListResponse getRecruitingCommissionList(Long designerId, Pageable pageable) {

		Designer designer = designerService.getById(designerId);
		Page<Commission> commissions = designerCommissionService.getRecruitingCommissions(pageable);

		return commissionMapper.toCommissionListResponse(commissions, designer.getLevel());
	}
}
