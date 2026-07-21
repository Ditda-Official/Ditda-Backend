package ditda.backend.domain.commission.draft.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;

public interface CommissionDraftFileRepository extends JpaRepository<CommissionDraftFile, Long> {

	@Query("select f from CommissionDraftFile f "
		+ "where f.commissionDraft.id in :draftIds "
		+ "and f.fileOrder = 0 ")
	List<CommissionDraftFile> findThumbnails(@Param("draftIds") List<Long> draftIds);

	List<CommissionDraftFile> findByCommissionDraftIdOrderByFileOrderAsc(Long draftId);

	@Query("SELECT f from CommissionDraftFile f "
		+ "WHERE f.commissionDraft.id = :draftId "
		+ "AND f.fileOrder = 0")
	Optional<CommissionDraftFile> findThumbnail(@Param("draftId") Long draftId);

	List<CommissionDraftFile> findAllByCommissionDraftIdAndWatermarkStatus(
		Long draftId,
		WatermarkStatus watermarkStatus
	);

	@Query("SELECT f.id FROM CommissionDraftFile f "
		+ "WHERE (f.watermarkStatus = :failed AND f.watermarkRetryCount < :maxRetry) "
		+ "OR (f.watermarkStatus = :processing "
		+ "    AND f.updatedAt < :stuckBefore AND f.watermarkRetryCount < :maxRetry) "
		+ "ORDER BY f.updatedAt ASC")
	List<Long> findWatermarkRetryTargetIds(
		@Param("failed") WatermarkStatus failed,
		@Param("processing") WatermarkStatus processing,
		@Param("maxRetry") int maxRetry,
		@Param("stuckBefore") LocalDateTime stuckBefore,
		Pageable pageable
	);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CommissionDraftFile f "
		+ "SET f.watermarkStatus = :processing, "
		+ "    f.watermarkRetryCount = f.watermarkRetryCount + 1, "
		+ "    f.updatedAt = :now "
		+ "WHERE f.id = :id "
		+ "AND f.watermarkRetryCount < :maxRetry "
		+ "AND (f.watermarkStatus = :failed "
		+ "     OR (f.watermarkStatus = :processing AND f.updatedAt < :stuckBefore))")
	int claimForRetry(
		@Param("id") Long id,
		@Param("processing") WatermarkStatus processing,
		@Param("failed") WatermarkStatus failed,
		@Param("maxRetry") int maxRetry,
		@Param("stuckBefore") LocalDateTime stuckBefore,
		@Param("now") LocalDateTime now
	);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE CommissionDraftFile f "
		+ "SET f.watermarkStatus = :failed, "
		+ "    f.updatedAt = :now "
		+ "WHERE f.watermarkStatus = :processing "
		+ "AND f.updatedAt < :stuckBefore "
		+ "AND f.watermarkRetryCount >= :maxRetry")
	int failExhaustedStuckFiles(
		@Param("failed") WatermarkStatus failed,
		@Param("processing") WatermarkStatus processing,
		@Param("maxRetry") int maxRetry,
		@Param("stuckBefore") LocalDateTime stuckBefore,
		@Param("now") LocalDateTime now
	);
}
