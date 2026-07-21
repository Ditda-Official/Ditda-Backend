package ditda.backend.global.notification;

import java.util.Map;

public record MailMessage(
	String type,
	String to,
	Map<String, Object> variables
) {
}
