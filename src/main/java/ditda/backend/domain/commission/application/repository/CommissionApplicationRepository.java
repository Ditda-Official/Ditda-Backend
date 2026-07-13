package ditda.backend.domain.commission.application.repository;

import java.util.Collection;
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
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.status <> :excludedStatus")
	List<CommissionApplication> findWithDesignerAndUserByCommissionIdAndStatusNot(
		@Param("commissionId") Long commissionId,
		@Param("excludedStatus") ApplicationStatus excludedStatus
	);

	Optional<CommissionApplication> findByCommissionIdAndDesignerIdAndStatusNot(
		Long commissionId,
		Long designerId,
		ApplicationStatus status
	);

	@Query("SELECT COUNT(ca) FROM CommissionApplication ca "
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.status = :status")
	long countByCommissionAndStatus(
		@Param("commissionId") Long commissionId,
		@Param("status") ApplicationStatus status
	);

	// 지원 중복(PENDING) 여부
	boolean existsByCommissionIdAndDesignerIdAndStatus(Long commissionId, Long designerId, ApplicationStatus status);

	// 특정 상태의 지원자 + designer fetch
	@Query("SELECT ca FROM CommissionApplication ca "
		+ "JOIN FETCH ca.designer "
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.status = :status")
	List<CommissionApplication> findWithDesignerByCommissionIdAndStatus(
		@Param("commissionId") Long commissionId,
		@Param("status") ApplicationStatus status
	);

	// 특정 상태의 지원자 + designer/user fetch
	@Query("SELECT ca FROM CommissionApplication ca "
		+ "JOIN FETCH ca.designer d "
		+ "JOIN FETCH d.user "
		+ "WHERE ca.commission.id = :commissionId "
		+ "AND ca.status = :status")
	List<CommissionApplication> findWithDesignerAndUserByCommissionIdAndStatus(
		@Param("commissionId") Long commissionId,
		@Param("status") ApplicationStatus status
	);

	int countByDesignerIdAndStatus(Long designerId, ApplicationStatus status);

	int countByDesignerIdAndStatusIn(Long designerId, Collection<ApplicationStatus> statuses);
}
