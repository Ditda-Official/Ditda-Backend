package ditda.backend.domain.commission.revision.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.revision.entity.RevisionDetail;

public interface RevisionDetailRepository extends JpaRepository<RevisionDetail, Long> {

	List<RevisionDetail> findAllByRevisionRequest_Id(Long revisionRequestId);
}
