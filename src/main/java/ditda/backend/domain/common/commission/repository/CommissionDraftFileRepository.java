package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.CommissionDraftFile;

public interface CommissionDraftFileRepository extends JpaRepository<CommissionDraftFile, Long> {
}
