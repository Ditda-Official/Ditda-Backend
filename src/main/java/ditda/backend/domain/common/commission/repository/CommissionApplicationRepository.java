package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionApplication;

public interface CommissionApplicationRepository extends JpaRepository<CommissionApplication, Long> {
}
