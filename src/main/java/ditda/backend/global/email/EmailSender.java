package ditda.backend.global.email;

import java.util.List;

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

	private static final String VERIFICATION_SUBJECT = "[DITDA] 이메일 인증 코드";
	private static final String VERIFICATION_TEMPLATE = "email/verificationCode";

	private static final String DESIGNER_SIGNUP_SUBJECT = "[DITDA] 새 디자이너 가입 검토 요청";
	private static final String DESIGNER_SIGNUP_TEMPLATE = "email/designer-signup-notification";

	private static final String LOGO_CID = "logoImage";
	private static final String LOGO_PATH = "email-images/logo.png";

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

			helper.addInline(LOGO_CID, new ClassPathResource(LOGO_PATH));

			mailSender.send(mimeMessage);

			log.info("Verification email sent. to={}", email);
		} catch (Exception e) {
			log.error("Verification email send failed. to={}", email, e);
		}
	}

	@Async
	public void sendDesignerSignupNotification(
		String toEmail,
		Long userId,
		String designerName,
		String designerEmail,
		List<String> portfolioUrls
	) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(toEmail);
			helper.setSubject(DESIGNER_SIGNUP_SUBJECT);
			helper.setText(buildDesignerSignupHtml(userId, designerName, designerEmail, portfolioUrls), true);

			helper.addInline(LOGO_CID, new ClassPathResource(LOGO_PATH));

			mailSender.send(mimeMessage);

			log.info("Designer signup notification email sent. designerEmail={}", designerEmail);
		} catch (Exception e) {
			log.error("Designer signup notification email send failed. designerEmail={}", designerEmail, e);
		}
	}

	private String buildHtml(String code) {
		Context context = new Context();
		context.setVariable("code", code);
		return templateEngine.process(VERIFICATION_TEMPLATE, context);
	}

	private String buildDesignerSignupHtml(Long userId, String name, String email, List<String> portfolioUrls) {
		Context context = new Context();
		context.setVariable("userId", userId);
		context.setVariable("name", name);
		context.setVariable("email", email);
		context.setVariable("portfolioUrls", portfolioUrls);
		return templateEngine.process(DESIGNER_SIGNUP_TEMPLATE, context);
	}
}
