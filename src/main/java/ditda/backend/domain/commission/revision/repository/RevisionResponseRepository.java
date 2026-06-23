package ditda.backend.domain.commission.revision.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.revision.entity.RevisionResponse;

public interface RevisionResponseRepository extends JpaRepository<RevisionResponse, Long> {

	Optional<RevisionResponse> findByProducedDraftId(Long producedDraftId);
}
