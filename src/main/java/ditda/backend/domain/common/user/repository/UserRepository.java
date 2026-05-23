package ditda.backend.domain.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
