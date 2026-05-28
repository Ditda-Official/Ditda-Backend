package ditda.backend.domain.common.commission.entity;

import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "commission_drafts")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionDraft extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_draft_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "revision_request_id")
	private RevisionRequest revisionRequest;

	@Column(name = "round", nullable = false)
	private int round;

	@Column(name = "designer_comment", length = 500)
	private String designerComment;

	@Builder.Default
	@Column(name = "is_final", nullable = false)
	private boolean isFinal = false;

	public static CommissionDraft create(
		Commission commission,
		RevisionRequest revisionRequest,
		int round,
		String designerComment
	) {
		return CommissionDraft.builder()
			.commission(commission)
			.revisionRequest(revisionRequest)
			.round(round)
			.designerComment(designerComment)
			.isFinal(false)
			.build();
	}

	public void markAsFinal() {
		this.isFinal = true;
	}
}
