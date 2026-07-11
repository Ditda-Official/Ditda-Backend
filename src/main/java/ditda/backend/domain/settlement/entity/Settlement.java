package ditda.backend.domain.settlement.entity;

import java.time.LocalDateTime;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.settlement.entity.enums.SettlementStatus;
import ditda.backend.domain.settlement.entity.enums.SettlementType;
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
@Table(name = "settlements",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_settlements_designer_id_commission_id_settlement_type",
		columnNames = {"designer_id", "commission_id", "settlement_type"}
	))
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designer_id", nullable = false)
	private Designer designer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "settlement_type", length = 20, nullable = false)
	private SettlementType settlementType;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private SettlementStatus status;

	@Column(name = "settled_at")
	private LocalDateTime settledAt;

	public static Settlement create(
		Designer designer,
		Commission commission,
		SettlementType settlementType,
		int amount
	) {
		return Settlement.builder()
			.designer(designer)
			.commission(commission)
			.settlementType(settlementType)
			.amount(amount)
			.status(SettlementStatus.PENDING)
			.build();
	}
}
