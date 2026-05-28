package ditda.backend.domain.common.auth.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

import ditda.backend.domain.common.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@Column(name = "session_id", nullable = false, length = 36)
	private String sessionId;

	// 다중 기기 허용을 위해 N:1
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "refresh_token_hash", nullable = false, length = 128)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	public static RefreshToken createRefreshToken(
		User user,
		String sessionId,
		String tokenHash,
		LocalDateTime expiresAt
	) {
		return RefreshToken.builder()
			.user(user)
			.sessionId(sessionId)
			.tokenHash(tokenHash)
			.expiresAt(expiresAt)
			.build();
	}

	public void rotate(String newTokenHash, LocalDateTime newExpiresAt) {
		this.tokenHash = newTokenHash;
		this.expiresAt = newExpiresAt;
	}

	public boolean belongsTo(Long userId) {
		return this.user.getId().equals(userId);
	}

	public boolean matchesHash(String tokenHash) {
		if (tokenHash == null) {
			return false;
		}
		return MessageDigest.isEqual(
			this.tokenHash.getBytes(StandardCharsets.UTF_8),
			tokenHash.getBytes(StandardCharsets.UTF_8)
		);
	}
}
