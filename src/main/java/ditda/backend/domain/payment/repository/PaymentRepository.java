package ditda.backend.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
