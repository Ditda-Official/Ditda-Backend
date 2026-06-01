package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionColor;

public interface CommissionColorRepository extends JpaRepository<CommissionColor, Long> {
}
