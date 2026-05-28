package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.PageType;
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
// 같은 페이지 종류 중복 X
@Table(
	name = "commission_required_pages",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_required_page_type",
			columnNames = {"commission_id", "page_type"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionRequiredPage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_required_page_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "page_type", length = 30, nullable = false)
	private PageType pageType;

	@Column(name = "page_description", length = 150)
	private String pageDescription;

	public static CommissionRequiredPage create(
		Commission commission,
		PageType pageType,
		String pageDescription
	) {
		return CommissionRequiredPage.builder()
			.commission(commission)
			.pageType(pageType)
			.pageDescription(pageDescription)
			.build();
	}
}
