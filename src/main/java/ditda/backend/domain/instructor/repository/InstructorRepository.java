package ditda.backend.domain.instructor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ditda.backend.domain.instructor.entity.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

	@Query("SELECT i FROM Instructor i "
		+ "JOIN FETCH i.user "
		+ "WHERE i.id = :instructorId")
	Optional<Instructor> findByIdWithUser(Long instructorId);
}
