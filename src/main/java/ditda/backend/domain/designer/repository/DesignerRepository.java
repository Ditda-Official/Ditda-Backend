package ditda.backend.domain.designer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ditda.backend.domain.designer.entity.Designer;

public interface DesignerRepository extends JpaRepository<Designer, Long> {

	@Query("SELECT d FROM Designer  d "
		+ "JOIN FETCH d.user "
		+ "WHERE d.id = :designerId")
	Optional<Designer> findByIdWithUser(Long designerId);
}
