package ditda.backend.domain.designer.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.designer.dto.response.DesignerStatsResponse;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.mapper.DesignerResponseMapper;
import ditda.backend.domain.designer.service.DesignerService;
import ditda.backend.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerFacade {

	private final DesignerService designerService;
	private final SettlementService settlementService;
	private final ApplicationService applicationService;
	private final DesignerResponseMapper designerResponseMapper;

	// 디자이너 통계 조회
	@Transactional(readOnly = true)
	public DesignerStatsResponse getDesignerStats(Long designerId) {

		Designer designer = designerService.getByIdWithUser(designerId);

		return designerResponseMapper.toDesignerStatsResponse(
			designer,
			// 지급 받은 금액
			settlementService.getTotalIncome(designerId),
			// 제출한 1차 시안 수
			applicationService.countSubmittedFirstDrafts(designerId),
			// 선택된 1차 시안 수
			applicationService.countSelected(designerId)
		);
	}
}
