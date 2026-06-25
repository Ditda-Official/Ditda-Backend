package ditda.backend.domain.commission.core.repository;

import java.time.LocalDateTime;
import java.util.Collection;

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
}
