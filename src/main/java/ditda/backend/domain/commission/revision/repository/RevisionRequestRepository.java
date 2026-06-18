package ditda.backend.domain.commission.revision.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.repository.projection.RevisionStatusView;

public interface RevisionRequestRepository extends JpaRepository<RevisionRequest, Long> {

	// 답변이 없는 수정 요청이 하나라도 있으면 submitted=true (전송 완료)
	// 미열람 답변이 하나라도 있으면 hasUpdated=true
	@Query("SELECT rr.commission.id AS commissionId,"
		+ "CASE WHEN COUNT(CASE WHEN resp.id IS NULL THEN 1 END) > 0 THEN true ELSE false END AS submitted, "
		+ "CASE WHEN COUNT(CASE WHEN resp.id IS NOT NULL AND resp.checked = false THEN 1 END) > 0 "
		+ "THEN true ELSE false END AS hasUpdated "
		+ "FROM RevisionRequest rr "
		+ "LEFT JOIN RevisionResponse resp ON resp.revisionRequest = rr "
		+ "WHERE rr.commission.id IN :commissionIds "
		+ "GROUP BY rr.commission.id")
	List<RevisionStatusView> findRevisionStatuses(@Param("commissionIds") List<Long> commissionIds);
}
