package ditda.backend.domain.common.payment.entity;

import java.time.LocalDateTime;

import ditda.backend.domain.common.commission.entity.Commission;
import ditda.backend.domain.common.payment.entity.enums.PaymentStatus;
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
@Table(name = "payments")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Column(name = "depositor_name", length = 50)
	private String depositorName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 30, nullable = false)
	private PaymentStatus status;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	public static Payment create(Commission commission, int amount) {
		return Payment.builder()
			.commission(commission)
			.amount(amount)
			.status(PaymentStatus.PENDING)
			.build();
	}
}
