package ditda.backend.domain.common.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.term.entity.PaymentTerm;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {
}
