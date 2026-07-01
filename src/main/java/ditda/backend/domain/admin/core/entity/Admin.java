package ditda.backend.domain.admin.core.entity;

import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private Long id;

	@Column(name = "username", nullable = false, unique = true, length = 20)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	public static Admin create(String username, String encodedPassword, String name) {
		return Admin.builder()
			.username(username)
			.password(encodedPassword)
			.name(name)
			.build();
	}
}
