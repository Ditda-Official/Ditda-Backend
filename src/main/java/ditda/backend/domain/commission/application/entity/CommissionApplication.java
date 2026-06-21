package ditda.backend.domain.commission.application.entity;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.exception.ApplicationErrorCode;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.global.apipayload.exception.GeneralException;
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
// 한 디자이너는 한 외주에 1번만 지원
@Table(
	name = "commission_applications",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_application",
			columnNames = {"commission_id", "designer_id"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionApplication extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_application_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designer_id", nullable = false)
	private Designer designer;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private ApplicationStatus status;

	public static CommissionApplication create(Commission commission, Designer designer) {
		return CommissionApplication.builder()
			.commission(commission)
			.designer(designer)
			.status(ApplicationStatus.PENDING)
			.build();
	}

	public void markDraftSelected() {
		if (status != ApplicationStatus.DRAFT_SUBMITTED) {
			throw new GeneralException(ApplicationErrorCode.INVALID_STATUS_FOR_DRAFT_SELECTION);
		}
		this.status = ApplicationStatus.DRAFT_SELECTED;
	}

	public void markDraftRejected() {
		if (status != ApplicationStatus.DRAFT_SUBMITTED) {
			throw new GeneralException(ApplicationErrorCode.INVALID_STATUS_FOR_DRAFT_REJECTION);
		}
		this.status = ApplicationStatus.DRAFT_REJECTED;
	}
}
