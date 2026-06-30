package ditda.backend.domain.commission.dashboard.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.dashboard.repository.projection.DraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.MatchingView;
import ditda.backend.domain.commission.dashboard.repository.projection.RevisingView;

public interface DashboardCommissionRepository extends Repository<Commission, Long> {

	// 시안 제출 현황: commission + DRAFT_SELECTING/DRAFT_SUBMITTED 개수
	@Query("SELECT c AS commission, "
		+ "COUNT(a.id) AS submissionCount "
		+ "FROM Commission c "
		+ "LEFT JOIN CommissionApplication a ON a.commission = c AND a.status = :applicationStatus "
		+ "WHERE c.instructor.id = :instructorId AND c.status = :commissionStatuses "
		+ "GROUP BY c.id "
		+ "ORDER BY c.firstDraftDeadline ASC")
	List<DraftSubmissionView> findDraftSubmissionViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatuses") Set<CommissionStatus> commissionStatuses,
		@Param("applicationStatus") ApplicationStatus applicationStatus
	);

	// 매칭 중인 외주: commission + distinct level 수 + PENDING 지원자 수
	@Query("SELECT c AS commission, "
		+ "COUNT(DISTINCT a.designer.level) AS distinctLevelCount, "
		+ "COUNT(a.id) AS totalCount "
		+ "FROM Commission c "
		+ "LEFT JOIN CommissionApplication a ON a.commission = c AND a.status = :applicationStatus "
		+ "WHERE c.instructor.id = :instructorId AND c.status = :commissionStatus "
		+ "GROUP BY c.id "
		+ "ORDER BY c.applicationDeadline ASC")
	List<MatchingView> findMatchingViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatus") CommissionStatus commissionStatus,
		@Param("applicationStatus") ApplicationStatus applicationStatus
	);

	// 수정 중인 외주: commission + submitted(응답 없는 요청 존재) + hasUpdated(미열람 응답 존재)
	@Query("SELECT c AS commission, "
		+ "CASE WHEN COUNT(CASE WHEN rr.id IS NOT NULL AND resp.id IS NULL THEN 1 END) > 0 "
		+ "     THEN true ELSE false END AS submitted, "
		+ "CASE WHEN COUNT(CASE WHEN resp.id IS NOT NULL AND resp.checked = false THEN 1 END) > 0 "
		+ "     THEN true ELSE false END AS hasUpdated "
		+ "FROM Commission c "
		+ "LEFT JOIN RevisionRequest rr ON rr.commission = c "
		+ "LEFT JOIN RevisionResponse resp ON resp.revisionRequest = rr "
		+ "WHERE c.instructor.id = :instructorId AND c.status = :commissionStatus "
		+ "GROUP BY c.id "
		+ "ORDER BY c.finalDeadline ASC")
	List<RevisingView> findRevisingViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatus") CommissionStatus commissionStatus
	);
}
