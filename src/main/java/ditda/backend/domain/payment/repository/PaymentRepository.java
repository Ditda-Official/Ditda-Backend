package ditda.backend.domain.payment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.payment.entity.enums.PaymentStatus;
import ditda.backend.domain.payment.repository.projection.CommissionPaidAmount;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByCommissionId(Long commissionId);

	@Query("SELECT p.commission.id AS commissionId, p.amount AS amount "
		+ "FROM Payment p "
		+ "WHERE p.commission.id IN :commissionIds "
		+ "AND p.status = :status")
	List<CommissionPaidAmount> findPaidAmounts(
		@Param("commissionIds") Collection<Long> commissionIds,
		@Param("status") PaymentStatus status
	);

	@Query("SELECT count(p) FROM Payment p "
		+ "WHERE p.commission.instructor.id = :instructorId "
		+ "AND p.status = :status")
	long countByInstructorIdAndStatus(
		@Param("instructorId") Long instructorId,
		@Param("status") PaymentStatus status
	);
}
