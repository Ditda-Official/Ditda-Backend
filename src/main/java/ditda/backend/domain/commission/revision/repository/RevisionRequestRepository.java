package ditda.backend.domain.commission.revision.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.revision.entity.RevisionRequest;

public interface RevisionRequestRepository extends JpaRepository<RevisionRequest, Long> {

	int countByCommissionId(Long commissionId);
}
