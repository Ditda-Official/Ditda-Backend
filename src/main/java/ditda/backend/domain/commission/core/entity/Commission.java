package ditda.backend.domain.commission.core.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.PageSize;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.instructor.entity.Instructor;
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

	// 시안 지원 제출 마감 ~ 1차 시안 마감 일수
	private static final int APPLICATION_DEADLINE_OFFSET_DAYS = 7;

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
	private PageSize pageSize;

	@Column(name = "additional_concept", length = 300)
	private String additionalConcept;

	@Enumerated(EnumType.STRING)
	@Column(name = "color_selection_mode", length = 30, nullable = false)
	private ColorSelectionMode colorSelectionMode;

	@Column(name = "first_draft_deadline", nullable = false)
	private LocalDate firstDraftDeadline;

	// 디자이너 지원 선착순 마감일
	@Column(name = "application_deadline", nullable = false)
	private LocalDate applicationDeadline;

	@Column(name = "final_deadline", nullable = false)
	private LocalDate finalDeadline;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30, nullable = false)
	private CommissionStatus status;

	@Column(name = "max_revision", nullable = false)
	private int maxRevision;

	@Column(name = "selected_at")
	private LocalDateTime selectedAt;

	public static Commission create(
		Instructor instructor,
		PlanCode planCode,
		String title,
		CategoryType categoryType,
		PageSize pageSize,
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
			.pageSize(pageSize)
			.additionalConcept(additionalConcept)
			.colorSelectionMode(colorSelectionMode)
			.firstDraftDeadline(firstDraftDeadline)
			.applicationDeadline(firstDraftDeadline.minusDays(APPLICATION_DEADLINE_OFFSET_DAYS))
			.finalDeadline(finalDeadline)
			.status(CommissionStatus.PENDING)
			.maxRevision(planCode.getBaseRevision())
			.build();
	}

	public boolean isDraftListViewable(int currentDraftCount, LocalDate currentDate) {

		boolean isFirstDeadlinePassed = currentDate.isAfter(firstDraftDeadline);
		return isFirstDeadlinePassed || currentDraftCount >= planCode.getDesignerCount();
	}

	public boolean isOwnedBy(Long instructorId) {
		return Objects.equals(instructor.getId(), instructorId);
	}

	public boolean isSelectable() {
		return status == CommissionStatus.IN_PROGRESS;
	}

	public boolean isDesignerSelected() {
		return assignedDesigner != null;
	}

	public void selectDesigner(Designer designer, LocalDateTime selectedAt) {

		this.assignedDesigner = designer;
		this.status = CommissionStatus.EDITING;
		this.selectedAt = selectedAt;
	}
}
