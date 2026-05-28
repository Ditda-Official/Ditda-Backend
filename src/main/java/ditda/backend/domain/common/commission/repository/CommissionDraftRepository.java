package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionDraft;

public interface CommissionDraftRepository extends JpaRepository<CommissionDraft, Long> {
}
