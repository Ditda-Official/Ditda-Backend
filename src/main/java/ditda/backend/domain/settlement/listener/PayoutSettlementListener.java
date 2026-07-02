package ditda.backend.domain.settlement.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.PayoutRequestedEvent;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.designer.repository.DesignerRepository;
import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementType;
import ditda.backend.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PayoutSettlementListener {

	private final SettlementRepository settlementRepository;
	private final DesignerRepository designerRepository;
	private final CommissionRepository commissionRepository;

	// payout 요청 시 정산(PENDING) 생성
	@Transactional(propagation = Propagation.MANDATORY)
	@EventListener
	public void onPayoutRequested(PayoutRequestedEvent event) {

		SettlementType type = toSettlementType(event.reason());
		Commission commission = commissionRepository.getReferenceById(event.commissionId());

		List<Settlement> settlements = event.payouts().stream()
			.map(payout -> Settlement.create(
				designerRepository.getReferenceById(payout.designerId()),
				commission,
				type,
				payout.amount()
			))
			.toList();

		settlementRepository.saveAll(settlements);
	}

	private SettlementType toSettlementType(PayoutRequestedEvent.PayoutReason reason) {
		return switch (reason) {
			case FINAL_COMPLETED_AUTO, FINAL_COMPLETED_MANUAL -> SettlementType.FINAL;
			case FINAL_CANCELLED_BY_DEADLINE, DRAFT_SELECTION_REJECTED -> SettlementType.BASE;
		};
	}
}
