package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionDraftPage;

public interface CommissionDraftPageRepository extends JpaRepository<CommissionDraftPage, Long> {
}
