package ditda.backend.domain.commission.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.repository.projection.ApplicationSubmissionCount;

public interface CommissionApplicationRepository extends JpaRepository<CommissionApplication, Long> {

	List<CommissionApplication> findByCommission_Id(Long commissionId);

	@Query("SELECT a.commission.id AS commissionId, COUNT(a) AS count "
		+ "FROM CommissionApplication a "
		+ "WHERE a.commission.id IN :commissionIds AND a.status = :status "
		+ "GROUP BY a.commission.id")
	List<ApplicationSubmissionCount> countByCommissionIdsAndStatus(
		@Param("commissionIds") List<Long> commissionIds,
		@Param("status") ApplicationStatus status);
}
