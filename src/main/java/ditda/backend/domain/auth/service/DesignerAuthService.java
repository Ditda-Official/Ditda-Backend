package ditda.backend.domain.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.TokenResult;
import ditda.backend.domain.auth.dto.request.DesignerSignupRequest;
import ditda.backend.domain.auth.event.DesignerSignedUpEvent;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.repository.DesignerRepository;
import ditda.backend.domain.designer.service.PortfolioService;
import ditda.backend.domain.term.dto.TermAgreement;
import ditda.backend.domain.term.service.TermService;
import ditda.backend.domain.user.entity.User;
import ditda.backend.domain.user.entity.enums.UserRole;
import ditda.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerAuthService {

	private static final String DEFAULT_PROFILE_IMAGE = "profile/default.png";

	private final DesignerRepository designerRepository;
	private final UserService userService;
	private final TermService termService;
	private final AuthService authService;
	private final PortfolioService portfolioService;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public AuthResult signup(DesignerSignupRequest request, List<String> portfolioKeys) {

		// 아이디 및 이메일 검증
		userService.validateUsernameAvailable(request.username());
		userService.validateEmailAvailable(request.email());

		// 유저 생성
		User user = userService.createUser(
			request.username(),
			passwordEncoder.encode(request.password()),
			request.name(),
			request.email(),
			DEFAULT_PROFILE_IMAGE,
			request.phone(),
			UserRole.DESIGNER,
			LocalDateTime.now()
		);

		// 약관 동의 여부 DB 저장
		termService.saveTerms(user, toAgreements(request.terms()));

		// Designer 생성 및 DB 저장
		Designer designer = Designer.createDesigner(
			user,
			request.bankAccount().bankName(),
			request.bankAccount().accountNumber(),
			request.bankAccount().accountHolder()
		);
		// 왜인지 모르겠지만 save로 했을때는 user만 저장. Designer는 저장 X
		designerRepository.saveAndFlush(designer);

		// 포트폴리오 S3 key를 DB에 일괄 저장
		portfolioService.savePortfolios(designer, portfolioKeys);

		// accessToken&refreshToken 발급
		TokenResult tokens = authService.issueTokens(user);

		// 관리자 알림 이벤트 발행
		eventPublisher.publishEvent(new DesignerSignedUpEvent(
			user.getId(),
			user.getName(),
			user.getEmail(),
			portfolioKeys
		));

		return new AuthResult(
			user.getId(),
			user.getName(),
			user.getProfileImage(),
			tokens.accessToken(),
			tokens.refreshToken());
	}

	private List<TermAgreement> toAgreements(List<DesignerSignupRequest.TermRequest> terms) {
		return terms.stream()
			.map(t -> new TermAgreement(t.type(), t.version(), t.isAgreed()))
			.toList();
	}
}
