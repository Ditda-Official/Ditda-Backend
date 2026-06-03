package ditda.backend.domain.commission.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.core.entity.CommissionFile;

public interface CommissionFileRepository extends JpaRepository<CommissionFile, Long> {
}
