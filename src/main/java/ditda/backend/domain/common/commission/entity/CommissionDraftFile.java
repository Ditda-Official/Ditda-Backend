package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.WatermarkStatus;
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
			name = "uk_commission_draft_file_order",
			columnNames = {"commission_draft_id", "file_order"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionDraftFile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_draft_file_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_draft_id", nullable = false)
	private CommissionDraft commissionDraft;

	@Column(name = "file_order", nullable = false)
	private int fileOrder;

	@Column(name = "file_name", length = 100, nullable = false)
	private String fileName;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "watermarked_file_url")
	private String watermarkedFileUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "watermark_status", length = 20, nullable = false)
	private WatermarkStatus watermarkStatus;

	public static CommissionDraftFile create(
		CommissionDraft commissionDraft,
		int fileOrder,
		String fileName,
		String fileUrl
	) {
		return CommissionDraftFile.builder()
			.commissionDraft(commissionDraft)
			.fileOrder(fileOrder)
			.fileName(fileName)
			.fileUrl(fileUrl)
			.watermarkStatus(WatermarkStatus.PROCESSING)
			.build();
	}
}
