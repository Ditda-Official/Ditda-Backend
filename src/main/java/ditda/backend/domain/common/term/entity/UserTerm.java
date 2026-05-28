package ditda.backend.domain.common.term.entity;

import ditda.backend.domain.common.term.entity.enums.TermType;
import ditda.backend.domain.common.user.entity.User;
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
@Table(name = "user_terms")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerm extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_term_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "term_type", nullable = false, length = 50)
	private TermType termType;

	@Column(name = "version", nullable = false, length = 20)
	private String version;

	@Column(name = "is_agreed", nullable = false)
	private boolean isAgreed;

	public static UserTerm createTerm(User user, TermType type, String version, boolean isAgreed) {
		return UserTerm.builder()
			.user(user)
			.termType(type)
			.version(version)
			.isAgreed(isAgreed)
			.build();
	}
}
