package ditda.backend.domain.commission.draft.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;

public interface CommissionDraftFileRepository extends JpaRepository<CommissionDraftFile, Long> {
}
