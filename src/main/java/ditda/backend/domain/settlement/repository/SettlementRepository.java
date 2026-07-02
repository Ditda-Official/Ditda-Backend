package ditda.backend.domain.settlement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Query(value = "SELECT s from Settlement s "
		+ "JOIN FETCH s.commission c "
		+ "WHERE s.designer.id = :designerId "
		+ "AND s.status = :status",
		countQuery = "SELECT COUNT(s) FROM Settlement s "
		+ "WHERE s.designer.id = :designerId "
		+ "AND s.status = :status")
	Page<Settlement> findByDesignerIdAndStatus(
		@Param("designerId") Long designerId,
		@Param("status") SettlementStatus status,
		Pageable pageable
	);
}
