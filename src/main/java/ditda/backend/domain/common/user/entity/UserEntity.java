package ditda.backend.domain.common.user.entity;

import java.time.LocalDateTime;

import ditda.backend.domain.common.user.entity.enums.UserRole;
import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "username", nullable = false, unique = true, length = 20)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "profile_image_url", nullable = false)
	private String profileImage;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	public static UserEntity createUser(
		String username,
		String password,
		String name,
		String email,
		String profileImage,
		String phone,
		UserRole role
	) {
		return UserEntity.builder()
			.username(username)
			.password(password)
			.name(name)
			.email(email)
			.profileImage(profileImage)
			.phone(phone)
			.role(role)
			.build();
	}
}
