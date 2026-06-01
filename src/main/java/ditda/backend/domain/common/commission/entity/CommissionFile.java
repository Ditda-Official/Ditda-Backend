package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.FileKind;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "commission_files")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionFile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_file_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "file_kind", length = 20, nullable = false)
	private FileKind fileKind;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "description", length = 300)
	private String description;

	public static CommissionFile create(
		Commission commission,
		FileKind fileKind,
		String fileUrl,
		String description
	) {
		return CommissionFile.builder()
			.commission(commission)
			.fileKind(fileKind)
			.fileUrl(fileUrl)
			.description(description)
			.build();
	}
}
