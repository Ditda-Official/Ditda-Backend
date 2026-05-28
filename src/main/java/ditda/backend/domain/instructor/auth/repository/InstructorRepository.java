package ditda.backend.domain.instructor.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.instructor.auth.entity.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
