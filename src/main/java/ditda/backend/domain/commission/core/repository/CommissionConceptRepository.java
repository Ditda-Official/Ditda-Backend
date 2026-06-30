package ditda.backend.domain.commission.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.core.entity.CommissionConcept;

public interface CommissionConceptRepository extends JpaRepository<CommissionConcept, Long> {

	List<CommissionConcept> findByCommissionId(Long commissionId);
}
