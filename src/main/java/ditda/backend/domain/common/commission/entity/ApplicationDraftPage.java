package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.DraftStatus;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_draft_pages")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDraftPage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "application_draft_page_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_application_id", nullable = false)
	private CommissionApplication application;

	@Column(name = "page_order", nullable = false)
	private int pageOrder;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "watermarked_file_url")
	private String watermarkedFileUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "draft_status", length = 20, nullable = false)
	private DraftStatus draftStatus;

	public static ApplicationDraftPage create(
		CommissionApplication application,
		int pageOrder,
		String name,
		String fileUrl
	) {
		return ApplicationDraftPage.builder()
			.application(application)
			.pageOrder(pageOrder)
			.name(name)
			.fileUrl(fileUrl)
			.draftStatus(DraftStatus.PROCESSING)
			.build();
	}

	public void completeWatermark(String watermarkedFileUrl) {
		this.watermarkedFileUrl = watermarkedFileUrl;
		this.draftStatus = DraftStatus.COMPLETED;
	}

	public void failWatermark() {
		this.draftStatus = DraftStatus.FAILED;
	}
}
