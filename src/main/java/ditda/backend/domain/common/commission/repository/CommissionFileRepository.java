package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionFile;

public interface CommissionFileRepository extends JpaRepository<CommissionFile, Long> {
}
