package ditda.backend.domain.designer.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.common.auth.dto.AuthResult;
import ditda.backend.domain.common.auth.service.AuthService;
import ditda.backend.domain.common.term.dto.TermAgreement;
import ditda.backend.domain.common.term.service.TermService;
import ditda.backend.domain.common.user.entity.User;
import ditda.backend.domain.common.user.entity.enums.UserRole;
import ditda.backend.domain.common.user.service.UserService;
import ditda.backend.domain.designer.auth.dto.DesignerAuthResult;
import ditda.backend.domain.designer.auth.dto.request.DesignerSignupRequest;
import ditda.backend.domain.designer.auth.entity.Designer;
import ditda.backend.domain.designer.auth.event.DesignerSignedUpEvent;
import ditda.backend.domain.designer.auth.repository.DesignerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerAuthService {

	private static final String DEFAULT_PROFILE_IMAGE = "";

	private final DesignerRepository designerRepository;
	private final UserService userService;
	private final TermService termService;
	private final AuthService authService;
	private final PortfolioService portfolioService;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public DesignerAuthResult signup(DesignerSignupRequest request, List<String> portfolioKeys) {

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
			request.bankAccount().accountHolder());
		designerRepository.save(designer);

		// 포트폴리오 S3 key를 DB에 일괄 저장
		portfolioService.savePortfolios(designer, portfolioKeys);

		// accessToken&refreshToken 발급
		AuthResult tokens = authService.issueTokens(user.getId());

		// 관리자 알림 이벤트 발행
		eventPublisher.publishEvent(new DesignerSignedUpEvent(
			user.getId(),
			user.getName(),
			user.getEmail(),
			portfolioKeys
		));

		return new DesignerAuthResult(user.getId(), tokens.accessToken(), tokens.refreshTokenCookie());
	}

	private List<TermAgreement> toAgreements(List<DesignerSignupRequest.TermRequest> terms) {
		return terms.stream()
			.map(t -> new TermAgreement(t.type(), t.version(), t.isAgreed()))
			.toList();
	}
}
