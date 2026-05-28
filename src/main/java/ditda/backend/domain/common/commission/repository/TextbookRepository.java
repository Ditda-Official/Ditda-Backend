package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.Textbook;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {
}
