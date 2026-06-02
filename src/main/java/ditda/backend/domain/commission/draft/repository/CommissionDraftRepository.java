package ditda.backend.domain.commission.draft.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.draft.entity.CommissionDraft;

public interface CommissionDraftRepository extends JpaRepository<CommissionDraft, Long> {
}
