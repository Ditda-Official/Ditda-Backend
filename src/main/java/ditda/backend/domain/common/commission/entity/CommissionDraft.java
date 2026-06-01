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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 한 지원에 같은 라운드 시안 1개만
@Table(
	name = "commission_drafts",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_draft_round",
			columnNames = {"commission_application_id", "round"}
		)
	}
)
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
	@JoinColumn(name = "commission_application_id", nullable = false)
	private CommissionApplication commissionApplication;

	@Column(name = "round", nullable = false)
	private int round;

	@Builder.Default
	@Column(name = "is_final", nullable = false)
	private boolean isFinal = false;

	// 1차 시안 (디자이너 지원 완료 후 제출, 수정 0회)
	public static CommissionDraft createInitial(CommissionApplication commissionApplication) {
		return CommissionDraft.builder()
			.commissionApplication(commissionApplication)
			.round(0)
			.build();
	}

	// 수정 시안 (강사의 수정요청에 대한 응답)
	public static CommissionDraft createRevision(
		CommissionApplication commissionApplication,
		int round
	) {
		return CommissionDraft.builder()
			.commissionApplication(commissionApplication)
			.round(round)
			.build();
	}
}
