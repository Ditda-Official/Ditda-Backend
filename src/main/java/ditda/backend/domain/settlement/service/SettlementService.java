package ditda.backend.domain.settlement.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.settlement.entity.enums.SettlementStatus;
import ditda.backend.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;

	@Transactional(readOnly = true)
	public long getTotalIncome(Long designerId) {
		return settlementRepository.sumAmountByDesignerIdAndStatus(
			designerId,
			SettlementStatus.COMPLETED
		);
	}
}
