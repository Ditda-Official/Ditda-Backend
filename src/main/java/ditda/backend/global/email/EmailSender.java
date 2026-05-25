package ditda.backend.global.email;

import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

	private static final String LOGO_CID = "logoImage";
	private static final String LOGO_PATH = "email-images/logo.png";

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	@Async
	public void send(String to, String subject, String templateName, Map<String, Object> variables) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(renderTemplate(templateName, variables), true);

			helper.addInline(LOGO_CID, new ClassPathResource(LOGO_PATH));

			mailSender.send(mimeMessage);

			log.info("Email sent. to={}, template={}", to, templateName);
		} catch (Exception e) {
			log.error("Email send failed. to={}, template={}", to, templateName, e);
		}
	}

	private String renderTemplate(String templateName, Map<String, Object> variables) {
		Context context = new Context();
		variables.forEach(context::setVariable);
		return templateEngine.process(templateName, context);
	}
}
