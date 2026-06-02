package ditda.backend.domain.designer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.designer.entity.Designer;

public interface DesignerRepository extends JpaRepository<Designer, Long> {
}
