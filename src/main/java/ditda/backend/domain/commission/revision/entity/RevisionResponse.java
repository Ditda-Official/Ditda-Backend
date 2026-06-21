package ditda.backend.domain.commission.revision.entity;

import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revision_responses")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionResponse extends BaseEntity {

	@Id
	@Column(name = "revision_request_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "revision_request_id")
	private RevisionRequest revisionRequest;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "produced_draft_id", nullable = false)
	private CommissionDraft producedDraft;

	@Column(name = "designer_comment", length = 500, nullable = false)
	private String designerComment;

	@Builder.Default
	@Column(name = "checked", nullable = false)
	private boolean checked = false;

	public static RevisionResponse create(
		RevisionRequest revisionRequest,
		CommissionDraft producedDraft,
		String designerComment
	) {
		return RevisionResponse.builder()
			.revisionRequest(revisionRequest)
			.producedDraft(producedDraft)
			.designerComment(designerComment)
			.build();
	}

	// 수정 답변을 확인 처리
	public void check() {
		this.checked = true;
	}
}
