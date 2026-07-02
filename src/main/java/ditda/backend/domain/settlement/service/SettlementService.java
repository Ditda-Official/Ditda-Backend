package ditda.backend.domain.settlement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementStatus;
import ditda.backend.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;

	// 정산 완료된 외주 개수 조회
	@Transactional(readOnly = true)
	public long getTotalIncome(Long designerId) {
		return settlementRepository.sumAmountByDesignerIdAndStatus(
			designerId,
			SettlementStatus.COMPLETED
		);
	}

	// 정산 완료된 외주 조회
	@Transactional(readOnly = true)
	public Page<Settlement> getCompletedSettlements(Long designerId, Pageable pageable) {
		return settlementRepository.findByDesignerIdAndStatus(
			designerId,
			SettlementStatus.COMPLETED,
			pageable
		);
	}
}
