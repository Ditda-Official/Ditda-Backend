package ditda.backend.domain.common.commission.entity;

import java.time.LocalDate;

import ditda.backend.domain.common.commission.entity.enums.CategoryType;
import ditda.backend.domain.common.commission.entity.enums.ColorSelectionMode;
import ditda.backend.domain.common.commission.entity.enums.CommissionStatus;
import ditda.backend.domain.common.commission.entity.enums.PlanCode;
import ditda.backend.domain.common.commission.entity.enums.Size;
import ditda.backend.domain.designer.auth.entity.Designer;
import ditda.backend.domain.instructor.auth.entity.Instructor;
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
@Table(name = "commissions")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Commission extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id", nullable = false)
	private Instructor instructor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_designer_id")
	private Designer assignedDesigner;

	@Enumerated(EnumType.STRING)
	@Column(name = "plan_code", length = 10, nullable = false)
	private PlanCode planCode;

	@Column(name = "title", length = 50, nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "category_type", length = 50, nullable = false)
	private CategoryType categoryType;

	@Enumerated(EnumType.STRING)
	@Column(name = "size", length = 10, nullable = false)
	private Size size;

	@Column(name = "additional_concept", length = 300)
	private String additionalConcept;

	@Enumerated(EnumType.STRING)
	@Column(name = "color_selection_mode", length = 30, nullable = false)
	private ColorSelectionMode colorSelectionMode;

	@Column(name = "first_draft_deadline", nullable = false)
	private LocalDate firstDraftDeadline;

	@Column(name = "application_deadline", nullable = false)
	private LocalDate applicationDeadline;

	@Column(name = "final_deadline", nullable = false)
	private LocalDate finalDeadline;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30, nullable = false)
	private CommissionStatus status;

	@Column(name = "max_revision", nullable = false)
	private int maxRevision;

	public static Commission create(
		Instructor instructor,
		PlanCode planCode,
		String title,
		CategoryType categoryType,
		Size size,
		String additionalConcept,
		ColorSelectionMode colorSelectionMode,
		LocalDate firstDraftDeadline,
		LocalDate finalDeadline
	) {
		return Commission.builder()
			.instructor(instructor)
			.planCode(planCode)
			.title(title)
			.categoryType(categoryType)
			.size(size)
			.additionalConcept(additionalConcept)
			.colorSelectionMode(colorSelectionMode)
			.firstDraftDeadline(firstDraftDeadline)
			.applicationDeadline(firstDraftDeadline.minusDays(7))
			.finalDeadline(finalDeadline)
			.status(CommissionStatus.PENDING)
			.maxRevision(planCode.getMaxRevision())
			.build();
	}
}
