package ditda.backend.domain.settlement.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementStatus;
import ditda.backend.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

	private final SettlementRepository settlementRepository;

	// 디자이너의 정산 완료 금액 합계
	public long getTotalIncome(Long designerId) {
		return settlementRepository.sumAmountByDesignerIdAndStatus(
			designerId,
			SettlementStatus.COMPLETED
		);
	}

	// 정산 완료된 외주 조회
	public Page<Settlement> getCompletedSettlements(Long designerId, Pageable pageable) {
		return settlementRepository.findByDesignerIdAndStatus(
			designerId,
			SettlementStatus.COMPLETED,
			pageable
		);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void createAll(List<Settlement> settlements) {
		settlementRepository.saveAll(settlements);
	}
}
