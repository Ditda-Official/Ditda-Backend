package ditda.backend.domain.common.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.user.entity.UserEntity;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<UserEntity> findByUsername(String username);
}
