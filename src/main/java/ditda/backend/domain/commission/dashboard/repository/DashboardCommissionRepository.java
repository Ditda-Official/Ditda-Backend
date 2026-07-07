package ditda.backend.domain.commission.dashboard.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerAnnouncementView;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerDraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerRevisingView;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorDraftSubmissionView;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorMatchingView;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorRevisingView;

public interface DashboardCommissionRepository extends Repository<Commission, Long> {

	// 강사 시안 제출 현황: commission + DRAFT_SUBMITTED 개수
	@Query("SELECT c AS commission, "
		+ "COUNT(a.id) AS submissionCount "
		+ "FROM Commission c "
		+ "LEFT JOIN CommissionApplication a ON a.commission = c AND a.status = :applicationStatus "
		+ "WHERE c.instructor.id = :instructorId AND c.status IN :commissionStatuses "
		+ "GROUP BY c.id "
		+ "ORDER BY c.firstDraftDeadline ASC")
	List<InstructorDraftSubmissionView> findInstructorDraftSubmissionViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatuses") Set<CommissionStatus> commissionStatuses,
		@Param("applicationStatus") ApplicationStatus applicationStatus
	);

	// 강사 매칭 중인 외주: commission + distinct level 수 + PENDING 지원자 수
	@Query("SELECT c AS commission, "
		+ "COUNT(DISTINCT d.level) AS distinctLevelCount, "
		+ "COUNT(a.id) AS totalCount "
		+ "FROM Commission c "
		+ "LEFT JOIN CommissionApplication a ON a.commission = c AND a.status = :applicationStatus "
		+ "LEFT JOIN a.designer d "
		+ "WHERE c.instructor.id = :instructorId AND c.status = :commissionStatus "
		+ "GROUP BY c.id "
		+ "ORDER BY c.applicationDeadline ASC")
	List<InstructorMatchingView> findInstructorMatchingViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatus") CommissionStatus commissionStatus,
		@Param("applicationStatus") ApplicationStatus applicationStatus
	);

	// 강사 수정 중인 외주: commission + submitted(응답 없는 요청 존재) + hasUpdated(미열람 응답 존재)
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
	List<InstructorRevisingView> findInstructorRevisingViews(
		@Param("instructorId") Long instructorId,
		@Param("commissionStatus") CommissionStatus commissionStatus
	);

	// 디자이너 시안 제출 예정 외주: commission + 지원 상태 + 디자이너 레벨
	@Query("SELECT c AS commission, ca.status AS applicationStatus, d.level AS designerLevel "
		+ "FROM CommissionApplication ca "
		+ "JOIN ca.commission c "
		+ "JOIN ca.designer d "
		+ "WHERE d.id = :designerId "
		+ "AND ca.status IN :applicationStatuses "
		+ "AND c.status = :commissionStatus "
		+ "ORDER BY c.firstDraftDeadline ASC")
	List<DesignerDraftSubmissionView> findDesignerDraftSubmissionViews(
		@Param("designerId") Long designerId,
		@Param("commissionStatus") CommissionStatus commissionStatus,
		@Param("applicationStatuses") Set<ApplicationStatus> applicationStatuses
	);

	// 디자이너 발표 대기 외주: commission + 지원 상태
	// ORDER BY CASE 우선순위는 AnnouncementResult 순서와 동기화 필요
	@Query("SELECT c AS commission, ca.status AS applicationStatus "
		+ "FROM CommissionApplication ca "
		+ "JOIN ca.commission c "
		+ "WHERE ca.designer.id = :designerId "
		+ "AND ca.status IN :applicationStatuses "
		+ "ORDER BY "
		+ "CASE "
		+ "WHEN ca.status = ApplicationStatus.PENDING THEN 0 "
		+ "WHEN ca.status = ApplicationStatus.ASSIGNED THEN 1 "
		+ "WHEN ca.status = ApplicationStatus.APPLICATION_REJECTED THEN 2 "
		+ "ELSE 3 END, "
		+ "c.applicationDeadline ASC")
	List<DesignerAnnouncementView> findDesignerAnnouncementViews(
		@Param("designerId") Long designerId,
		@Param("applicationStatuses") Set<ApplicationStatus> applicationStatuses
	);

	// 디자이너 수정 중인 외주: commission + submitted(미응답 요청 없음) + hasUpdated(미열람 수정 요청 존재)
	@Query("SELECT c AS commission, "
		+ "CASE WHEN COUNT(CASE WHEN rr.id IS NOT NULL AND resp.id IS NULL THEN 1 END) = 0 "
		+ "     THEN true ELSE false END AS submitted, "
		+ "CASE WHEN COUNT(CASE WHEN rr.id IS NOT NULL AND rr.checked = false THEN 1 END) > 0 "
		+ "     THEN true ELSE false END AS hasUpdated "
		+ "FROM Commission c "
		+ "LEFT JOIN RevisionRequest rr ON rr.commission = c "
		+ "LEFT JOIN RevisionResponse resp ON resp.revisionRequest = rr "
		+ "WHERE c.assignedDesigner.id = :designerId AND c.status = :commissionStatus "
		+ "GROUP BY c.id "
		+ "ORDER BY c.finalDeadline ASC")
	List<DesignerRevisingView> findDesignerRevisingViews(
		@Param("designerId") Long designerId,
		@Param("commissionStatus") CommissionStatus commissionStatus
	);
}
