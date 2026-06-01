package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.ColorRole;
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
// 하나의 외주에 MAIN/SUB1/SUB2 각 1개씩만
@Table(
	name = "commission_colors",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_commission_color_role",
			columnNames = {"commission_id", "role"}
		)
	}
)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionColor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_color_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 10, nullable = false)
	private ColorRole role;

	@Column(name = "color_code", length = 7, nullable = false)
	private String colorCode;

	public static CommissionColor create(
		Commission commission,
		ColorRole role,
		String colorCode
	) {
		return CommissionColor.builder()
			.commission(commission)
			.role(role)
			.colorCode(colorCode)
			.build();
	}
}
