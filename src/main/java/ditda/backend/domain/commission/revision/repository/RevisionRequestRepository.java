package ditda.backend.domain.commission.revision.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.revision.entity.RevisionRequest;

public interface RevisionRequestRepository extends JpaRepository<RevisionRequest, Long> {

	int countByCommissionId(Long commissionId);

	boolean existsByTargetDraftId(Long targetDraftId);

	Optional<RevisionRequest> findByTargetDraftId(Long targetDraftId);
}
