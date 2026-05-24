package ditda.backend.domain.designer.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.designer.auth.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
