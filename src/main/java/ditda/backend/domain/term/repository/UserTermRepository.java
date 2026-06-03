package ditda.backend.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ditda.backend.domain.term.entity.UserTerm;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {
}
