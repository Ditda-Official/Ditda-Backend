package ditda.backend.domain.commission.draft.repository;

import java.util.List;
import java.util.Optional;

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

	@Query("select count(d) from CommissionDraft d "
		+ "join d.commissionApplication ca "
		+ "where ca.commission.id = :commissionId "
		+ "and d.round = 0")
	int countFirstRoundDrafts(@Param("commissionId") Long commissionId);

	@Query("select d from CommissionDraft d "
		+ "join d.commissionApplication ca "
		+ "where d.id = :draftId "
		+ "and ca.commission.id = :commissionId")
	Optional<CommissionDraft> findDraftInCommission(
		@Param("draftId") Long draftId,
		@Param("commissionId") Long commissionId
	);

	@Query("select count(d) > 0 from CommissionDraft d "
		+ "join d.commissionApplication ca "
		+ "where d.id = :draftId "
		+ "and ca.commission.id = :commissionId")
	boolean existsDraftInCommission(
		@Param("draftId") Long draftId,
		@Param("commissionId") Long commissionId
	);
}
