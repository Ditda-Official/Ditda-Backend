package ditda.backend.global.email;

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

	private static final String VERIFICATION_SUBJECT = "[Ditda] 이메일 인증 코드";
	private static final String VERIFICATION_TEMPLATE = "email/verificationCode";

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	@Async
	public void sendVerificationEmail(String email, String code) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(email);
			helper.setSubject(VERIFICATION_SUBJECT);
			helper.setText(buildHtml(code), true);

			mailSender.send(mimeMessage);

			log.info("Verification email sent. to={}", email);
		} catch (Exception e) {
			log.error("Verification email send failed. to={}", email, e);
		}
	}

	private String buildHtml(String code) {
		Context context = new Context();
		context.setVariable("code", code);
		return templateEngine.process(VERIFICATION_TEMPLATE, context);
	}
}