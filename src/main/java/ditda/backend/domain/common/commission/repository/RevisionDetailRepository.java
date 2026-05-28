package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.RevisionDetail;

public interface RevisionDetailRepository extends JpaRepository<RevisionDetail, Long> {
}
