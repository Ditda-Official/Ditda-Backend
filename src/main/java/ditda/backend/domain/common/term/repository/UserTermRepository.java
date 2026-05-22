package ditda.backend.domain.common.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.common.term.entity.UserTerm;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {
}
