package ditda.backend.domain.settlement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementStatus;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

	@Query("SELECT COALESCE(SUM(s.amount), 0) FROM Settlement s "
		+ "WHERE s.designer.id = :designerId "
		+ "AND s.status = :status")
	long sumAmountByDesignerIdAndStatus(
		@Param("designerId") Long designerId,
		@Param("status") SettlementStatus status
	);
}
