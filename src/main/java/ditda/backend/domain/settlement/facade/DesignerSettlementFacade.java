package ditda.backend.domain.settlement.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.settlement.dto.response.DesignerSettlementItemResponse;
import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.service.SettlementService;
import ditda.backend.global.apipayload.response.PageResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerSettlementFacade {

	private final SettlementService settlementService;

	@Transactional(readOnly = true)
	public PageResponse<DesignerSettlementItemResponse> getDesignerSettlements(Long designerId, Pageable pageable) {

		Page<Settlement> page = settlementService.getCompletedSettlements(designerId, pageable);
		return PageResponse.from(page.map(DesignerSettlementItemResponse::from));
	}
}
