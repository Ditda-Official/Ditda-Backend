package ditda.backend.domain.commission.history.facade;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.history.dto.response.InstructorCommissionItemResponse;
import ditda.backend.domain.commission.history.service.CommissionHistoryService;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.global.apipayload.response.PageResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorCommissionHistoryFacade {

	private final CommissionHistoryService commissionHistoryService;
	private final PaymentService paymentService;

	@Transactional(readOnly = true)
	public PageResponse<InstructorCommissionItemResponse> getInstructorCommissions(
		Long instructorId,
		Pageable pageable
	) {

		Page<Commission> page = commissionHistoryService.getInstructorCommissions(instructorId, pageable);

		List<Long> commissionIds = page.getContent().stream()
			.map(Commission::getId)
			.toList();

		// 실제 결제된 가격 조회
		Map<Long, Integer> paidAmounts = paymentService.getPaidAmounts(commissionIds);

		return PageResponse.of(page,
			c -> InstructorCommissionItemResponse.from(c, paidAmounts.get(c.getId())));
	}
}
