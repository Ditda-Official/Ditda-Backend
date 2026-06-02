package ditda.backend.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.term.entity.PaymentTerm;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {
}
