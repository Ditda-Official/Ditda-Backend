package ditda.backend.domain.commission.revision.entity;

import ditda.backend.domain.commission.revision.entity.enums.RevisionCategory;
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
@Table(name = "revision_details")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionDetail extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "revision_detail_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "revision_request_id", nullable = false)
	private RevisionRequest revisionRequest;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", length = 20, nullable = false)
	private RevisionCategory category;

	@Column(name = "comment", length = 300, nullable = false)
	private String comment;

	public static RevisionDetail create(
		RevisionRequest revisionRequest,
		RevisionCategory category,
		String comment
	) {
		return RevisionDetail.builder()
			.revisionRequest(revisionRequest)
			.category(category)
			.comment(comment)
			.build();
	}
}
