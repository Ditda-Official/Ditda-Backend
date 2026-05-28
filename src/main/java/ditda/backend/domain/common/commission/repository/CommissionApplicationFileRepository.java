package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionApplicationFile;

public interface CommissionApplicationFileRepository extends JpaRepository<CommissionApplicationFile, Long> {
}
