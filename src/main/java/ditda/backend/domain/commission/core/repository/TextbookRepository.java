package ditda.backend.domain.commission.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.core.entity.Textbook;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {
}
