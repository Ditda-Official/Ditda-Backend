package ditda.backend.domain.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
