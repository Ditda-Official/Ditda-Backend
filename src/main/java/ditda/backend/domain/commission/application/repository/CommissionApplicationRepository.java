package ditda.backend.domain.commission.application.repository;

import java.util.List;
import java.util.Optional;

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

	@Query("SELECT ca FROM CommissionApplication ca "
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.designer.id = :designerId")
	Optional<CommissionApplication> findByCommissionAndDesigner(
		@Param("commissionId") Long commissionId,
		@Param("designerId") Long designerId
	);

	@Query("SELECT COUNT(ca) FROM CommissionApplication ca "
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.status = :status")
	long countByCommissionAndStatus(
		@Param("commissionId") Long commissionId,
		@Param("status") ApplicationStatus status
	);
}
