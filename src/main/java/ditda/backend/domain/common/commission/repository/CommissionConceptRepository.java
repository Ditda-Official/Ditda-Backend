package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionConcept;

public interface CommissionConceptRepository extends JpaRepository<CommissionConcept, Long> {
}
