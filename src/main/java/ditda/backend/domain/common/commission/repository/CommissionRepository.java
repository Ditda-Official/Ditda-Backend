package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.Commission;

public interface CommissionRepository extends JpaRepository<Commission, Long> {
}
