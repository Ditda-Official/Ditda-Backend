package ditda.backend.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.payment.entity.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByCommissionId(Long commissionId);

	@Query("select count(p) from Payment p "
		+ "where p.commission.instructor.id = :instructorId "
		+ "and p.status = :status")
	long countByInstructorIdAndStatus(
		@Param("instructorId") Long instructorId,
		@Param("status") PaymentStatus status
	);
}
