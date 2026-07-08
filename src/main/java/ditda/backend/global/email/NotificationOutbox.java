package ditda.backend.global.email;

import java.time.LocalDateTime;
import java.util.Map;

import ditda.backend.global.converter.MapToJsonConverter;
import ditda.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_outboxes")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_outbox_id")
	private Long id;

	@Column(name = "recipient_email", nullable = false, length = 100)
	private String recipientEmail;

	@Column(name = "subject", nullable = false, length = 150)
	private String subject;

	@Column(name = "template_name", nullable = false, length = 100)
	private String templateName;

	@Convert(converter = MapToJsonConverter.class)
	@Column(name = "template_variables", nullable = false, columnDefinition = "TEXT")
	private Map<String, Object> templateVariables;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private OutboxStatus status;

	@Column(name = "scheduled_at", nullable = false)
	private LocalDateTime scheduledAt;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	public static NotificationOutbox create(
		String recipientEmail,
		NotificationType type,
		Map<String, Object> templateVariables,
		LocalDateTime scheduledAt
	) {
		return NotificationOutbox.builder()
			.recipientEmail(recipientEmail)
			.subject(type.getSubject())
			.templateName(type.getTemplate())
			.templateVariables(templateVariables)
			.status(OutboxStatus.PENDING)
			.scheduledAt(scheduledAt)
			.retryCount(0)
			.build();
	}

	public void markSent() {
		this.status = OutboxStatus.SENT;
		this.errorMessage = null;
		this.sentAt = LocalDateTime.now();
	}

	public void recordRetry(String errorMessage) {
		this.retryCount++;
		this.errorMessage = errorMessage;

		if (this.retryCount >= 3) {
			this.status = OutboxStatus.FAILED;
		}
	}
}
