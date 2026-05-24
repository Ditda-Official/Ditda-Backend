package ditda.backend.domain.common.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ditda.backend.domain.common.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	Optional<RefreshToken> findBySessionId(String sessionId);

	void deleteBySessionId(String sessionId);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt < :now")
	void deleteExpiredByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime expiresAtBefore);
}
