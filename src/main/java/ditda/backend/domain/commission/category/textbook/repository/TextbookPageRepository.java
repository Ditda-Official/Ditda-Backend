package ditda.backend.domain.commission.category.textbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.commission.category.textbook.entity.TextbookPage;

public interface TextbookPageRepository extends JpaRepository<TextbookPage, Long> {
}
