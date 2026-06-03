package ditda.backend.domain.designer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.designer.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
