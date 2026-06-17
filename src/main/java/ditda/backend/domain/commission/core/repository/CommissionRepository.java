package ditda.backend.domain.commission.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

	List<Commission> findByInstructorIdAndStatus(Long instructorId, CommissionStatus status);
}
