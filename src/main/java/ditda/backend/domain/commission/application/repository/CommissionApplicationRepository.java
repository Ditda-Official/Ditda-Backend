package ditda.backend.domain.commission.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.application.entity.CommissionApplication;

public interface CommissionApplicationRepository extends JpaRepository<CommissionApplication, Long> {
}
