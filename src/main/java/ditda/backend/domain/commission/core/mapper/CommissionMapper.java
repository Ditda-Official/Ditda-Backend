package ditda.backend.domain.commission.core.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.response.CommissionListResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionAmountPolicy;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;

@Component
public class CommissionMapper {

	public CommissionListResponse toCommissionListResponse(Page<Commission> page, DesignerLevel level) {

		CommissionAmountPolicy policy = CommissionAmountPolicy.from(level);
		int baseAmount = policy.getBaseAmount();
		int maxAmount = policy.getMaxAmount();

		List<CommissionListResponse.CommissionResponse> commissions = page.getContent().stream()
			.map(c -> new CommissionListResponse.CommissionResponse(
				c.getId(),
				c.getApplicationDeadline(),
				c.getCategoryType(),
				c.getTitle(),
				baseAmount,
				maxAmount))
			.toList();

		return new CommissionListResponse(
			commissions,
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}
