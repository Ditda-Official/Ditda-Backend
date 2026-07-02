package ditda.backend.domain.commission.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;

public interface CommissionApplicationRepository extends JpaRepository<CommissionApplication, Long> {

	@Query("SELECT ca from CommissionApplication ca "
		+ "JOIN FETCH ca.designer d "
		+ "JOIN FETCH d.user "
		+ "WHERE ca.commission.id = :commissionId")
	List<CommissionApplication> findWithDesignerAndUserByCommissionId(@Param("commissionId") Long commissionId);

	@Query("SELECT count(ca) from CommissionApplication ca "
		+ "WHERE ca.designer.id = :designerId "
		+ "AND ca.status = :status")
	int countByDesignerIdAndStatus(
		@Param("designerId") Long designerId,
		@Param("status") ApplicationStatus status
	);
}
