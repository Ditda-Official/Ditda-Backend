package ditda.backend.domain.commission.history.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import ditda.backend.domain.commission.core.entity.Commission;

public interface CommissionHistoryRepository extends Repository<Commission, Long> {

	Page<Commission> findByInstructorIdOrderByCreatedAtDescIdAsc(Long instructorId, Pageable pageable);
}
