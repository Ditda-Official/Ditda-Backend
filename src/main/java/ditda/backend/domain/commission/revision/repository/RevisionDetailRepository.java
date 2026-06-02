package ditda.backend.domain.commission.revision.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.revision.entity.RevisionDetail;

public interface RevisionDetailRepository extends JpaRepository<RevisionDetail, Long> {
}
