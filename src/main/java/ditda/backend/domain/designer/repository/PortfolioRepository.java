package ditda.backend.domain.designer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.designer.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	List<Portfolio> findByDesignerIdOrderByIdAsc(Long designerId);
}
