package ditda.backend.global.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

	@Query("SELECT o from NotificationOutbox o "
		+ "WHERE o.status = :status "
		+ "AND o.scheduledAt <= :now "
		+ "ORDER BY o.scheduledAt ASC")
	List<NotificationOutbox> findPendingScheduled(
		@Param("status") OutboxStatus status,
		@Param("now") LocalDateTime now,
		Limit limit
	);
}
