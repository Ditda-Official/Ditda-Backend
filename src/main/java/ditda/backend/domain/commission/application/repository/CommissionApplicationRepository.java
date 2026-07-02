package ditda.backend.domain.commission.application.repository;

import java.util.Collection;
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

	int countByDesignerIdAndStatus(Long designerId, ApplicationStatus status);

	int countByDesignerIdAndStatusIn(Long designerId, Collection<ApplicationStatus> statuses);
}
