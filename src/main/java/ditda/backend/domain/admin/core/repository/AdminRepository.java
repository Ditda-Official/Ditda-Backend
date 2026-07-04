package ditda.backend.domain.admin.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.admin.core.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	Optional<Admin> findByUsername(String username);
}
