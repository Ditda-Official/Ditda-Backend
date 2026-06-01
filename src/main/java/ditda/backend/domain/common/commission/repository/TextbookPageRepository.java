package ditda.backend.domain.common.commission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.commission.entity.TextbookPage;

public interface TextbookPageRepository extends JpaRepository<TextbookPage, Long> {
}
