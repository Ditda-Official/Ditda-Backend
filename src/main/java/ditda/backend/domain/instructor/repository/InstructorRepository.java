package ditda.backend.domain.instructor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.instructor.entity.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
