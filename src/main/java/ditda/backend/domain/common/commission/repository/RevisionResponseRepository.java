package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.RevisionResponse;

public interface RevisionResponseRepository extends JpaRepository<RevisionResponse, Long> {
}
