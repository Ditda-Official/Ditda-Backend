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
	name = "commission_application_files",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_application_file_order",
			columnNames = {"commission_application_id", "page_order"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionApplicationFile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_application_file_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_application_id", nullable = false)
	private CommissionApplication commissionApplication;

	@Column(name = "page_order", nullable = false)
	private int pageOrder;

	@Column(name = "file_name", length = 100, nullable = false)
	private String fileName;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "watermarked_file_url")
	private String watermarkedFileUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "watermark_status", length = 20, nullable = false)
	private WatermarkStatus watermarkStatus;

	public static CommissionApplicationFile create(
		CommissionApplication commissionApplication,
		int pageOrder,
		String fileName,
		String fileUrl
	) {
		return CommissionApplicationFile.builder()
			.commissionApplication(commissionApplication)
			.pageOrder(pageOrder)
			.fileName(fileName)
			.fileUrl(fileUrl)
			.watermarkStatus(WatermarkStatus.PROCESSING)
			.build();
	}
}
