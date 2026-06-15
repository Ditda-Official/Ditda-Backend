package ditda.backend.domain.commission.draft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.draft.entity.CommissionDraft;

public interface CommissionDraftRepository extends JpaRepository<CommissionDraft, Long> {

	@Query("select d from CommissionDraft d "
		+ "join d.commissionApplication ca "
		+ "where ca.commission.id = :commissionId "
		+ "and d.round = 0 "
		+ "order by d.id asc")
	List<CommissionDraft> findFirstRoundDrafts(@Param("commissionId") Long commissionId);

	boolean existsByIdAndCommissionApplication_Commission_Id(Long draftId, Long commissionId);
}
