package ditda.backend.domain.commission.draft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;

public interface CommissionDraftFileRepository extends JpaRepository<CommissionDraftFile, Long> {

	@Query("select f from CommissionDraftFile f "
		+ "where f.commissionDraft.id in :draftIds "
		+ "and f.fileOrder = 0 ")
	List<CommissionDraftFile> findThumbnails(@Param("draftIds") List<Long> draftIds);

	List<CommissionDraftFile> findByCommissionDraftIdOrderByFileOrderAsc(Long draftId);
}
