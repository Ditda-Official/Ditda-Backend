package ditda.backend.domain.common.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
