package ditda.backend.domain.designer.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.designer.auth.entity.Designer;

public interface DesignerRepository extends JpaRepository<Designer, Long> {
}
