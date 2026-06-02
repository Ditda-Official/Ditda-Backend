package ditda.backend.domain.designer.entity;

import ditda.backend.domain.designer.entity.enums.BankName;
import ditda.backend.domain.user.entity.User;
import ditda.backend.global.encryption.AesEncryptConverter;
import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "designers")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Designer extends BaseEntity {

	@Id
	@Column(name = "designer_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "designer_id")
	private User user;

	@Builder.Default
	@Column(name = "level", nullable = false)
	private int level = 1;

	@Builder.Default
	@Column(name = "exp", nullable = false)
	private int exp = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "bank_name", length = 50, nullable = false)
	private BankName bankName;

	@Convert(converter = AesEncryptConverter.class)
	@Column(name = "account_number", nullable = false)
	private String accountNumber;

	@Column(name = "account_holder", length = 50, nullable = false)
	private String accountHolder;

	public static Designer createDesigner(User user, BankName bankName, String accountNumber, String accountHolder) {
		return Designer.builder()
			.user(user)
			.bankName(bankName)
			.accountNumber(accountNumber)
			.accountHolder(accountHolder)
			.build();
	}
}
