package ditda.backend.global.logging;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMasker {

	public static String maskEmail(String email) {
		if (!StringUtils.hasText(email) || !email.contains("@")) {
			return "***";
		}

		int at = email.indexOf("@");
		String local = email.substring(0, at);
		String domain = email.substring(at);

		if (local.length() <= 2) {
			return "****" + domain;
		}

		return local.substring(0, 2) + "****" + domain;
	}
}
