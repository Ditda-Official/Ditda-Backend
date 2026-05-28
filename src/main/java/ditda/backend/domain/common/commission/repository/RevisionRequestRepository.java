package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.RevisionRequest;

public interface RevisionRequestRepository extends JpaRepository<RevisionRequest, Long> {
}
