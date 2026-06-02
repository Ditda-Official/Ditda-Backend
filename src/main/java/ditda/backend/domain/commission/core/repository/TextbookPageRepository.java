package ditda.backend.domain.commission.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.core.entity.TextbookPage;

public interface TextbookPageRepository extends JpaRepository<TextbookPage, Long> {
}
