package ditda.backend.domain.commission.core.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.designer.entity.Designer;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

	@Modifying
	@Query("update Commission c "
		+ "set c.assignedDesigner = :designer, "
		+ "c.status = CommissionStatus.EDITING, "
		+ "c.selectedAt = :now "
		+ "where c.id = :commissionId "
		+ "and c.status = CommissionStatus.DRAFT_SELECTING "
		+ "and c.assignedDesigner is null")
	int selectDesignerIfAvailable(
		@Param("commissionId") Long id,
		@Param("designer") Designer designer,
		@Param("now") LocalDateTime now
	);

	long countByInstructorIdAndStatusIn(Long instructorId, Collection<CommissionStatus> statuses);

	@Query("SELECT c from Commission c "
		+ "WHERE c.applicationDeadline < :today "
		+ "AND c.status = :status")
	List<Commission> findByStatusAndApplicationDeadlineBefore(
		@Param("status") CommissionStatus status,
		@Param("today") LocalDate today
	);

	@Query("SELECT c from Commission c "
		+ "WHERE c.firstDraftDeadline < :today "
		+ "AND c.status = :status")
	List<Commission> findByStatusAndFirstDraftDeadlineBefore(
		@Param("status") CommissionStatus status,
		@Param("today") LocalDate today
	);

	@Query("SELECT c FROM Commission c "
		+ "WHERE c.finalDeadline < :today "
		+ "AND c.status IN :statuses")
	List<Commission> findByStatusInAndFinalDeadlineBefore(
		@Param("statuses") Collection<CommissionStatus> statuses,
		@Param("today") LocalDate today
	);

	@Query("SELECT c from Commission c "
		+ "JOIN FETCH c.instructor i "
		+ "JOIN FETCH i.user "
		+ "WHERE c.id = :commissionId")
	Optional<Commission> findWithInstructorAndUserById(@Param("commissionId") Long commissionId);

	@Query("SELECT c FROM Commission c "
		+ "JOIN FETCH c.instructor i "
		+ "JOIN FETCH i.user "
		+ "LEFT JOIN FETCH c.assignedDesigner d "
		+ "LEFT JOIN FETCH d.user "
		+ "WHERE c.id = :commissionId")
	Optional<Commission> findWithInstructorAndAssignedDesignerById(@Param("commissionId") Long commissionId);

	Page<Commission> findByStatus(CommissionStatus status, Pageable pageable);
}
