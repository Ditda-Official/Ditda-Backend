package ditda.backend.domain.commission.draft.entity;

import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 한 시안의 페이지 순서 중복 X
@Table(
	name = "commission_draft_files",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_draft_files_commission_draft_id_file_order",
			columnNames = {"commission_draft_id", "file_order"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionDraftFile extends BaseEntity {

	public static final int MAX_WATERMARK_RETRY = 3;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_draft_file_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_draft_id", nullable = false)
	private CommissionDraft commissionDraft;

	@Column(name = "file_order", nullable = false)
	private int fileOrder;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "watermarked_file_url")
	private String watermarkedFileUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "watermark_status", length = 20, nullable = false)
	private WatermarkStatus watermarkStatus;

	@Column(name = "watermark_retry_count", nullable = false)
	private int watermarkRetryCount;

	public static CommissionDraftFile create(
		CommissionDraft commissionDraft,
		int fileOrder,
		String fileUrl
	) {
		return CommissionDraftFile.builder()
			.commissionDraft(commissionDraft)
			.fileOrder(fileOrder)
			.fileUrl(fileUrl)
			.watermarkStatus(WatermarkStatus.PROCESSING)
			.build();
	}

	// 시안 조회 가능 여부 (워터마크 완료 상태)
	public boolean isWatermarkCompleted() {
		return this.watermarkStatus == WatermarkStatus.COMPLETED;
	}

	// 워터마크 처리 완료
	public void completeWatermark(String watermarkedFileUrl) {
		this.watermarkedFileUrl = watermarkedFileUrl;
		this.watermarkStatus = WatermarkStatus.COMPLETED;
	}

	// 워터마크 처리 실패
	public void markWatermarkFailed() {
		this.watermarkStatus = WatermarkStatus.FAILED;
	}

	// 재시도 가능 여부 확인
	public boolean isWatermarkRetryable() {
		return watermarkRetryCount < MAX_WATERMARK_RETRY;
	}

	// 재시도 불가 실패 (이미지 문제)
	public void markWatermarkFailedPermanently() {
		this.watermarkRetryCount = MAX_WATERMARK_RETRY;
		this.watermarkStatus = WatermarkStatus.FAILED;
	}
}
