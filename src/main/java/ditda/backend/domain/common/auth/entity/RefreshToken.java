package ditda.backend.domain.common.auth.entity;

import java.time.LocalDateTime;

import ditda.backend.domain.common.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	private Long userId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "refresh_token", nullable = false, length = 512)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	public static RefreshToken createRefreshToken(User user, String token, LocalDateTime expiresAt) {
		return RefreshToken.builder()
			.user(user)
			.token(token)
			.expiresAt(expiresAt)
			.build();
	}

	public void rotate(String newToken, LocalDateTime newExpiresAt) {
		this.token = newToken;
		this.expiresAt = newExpiresAt;
	}
}
