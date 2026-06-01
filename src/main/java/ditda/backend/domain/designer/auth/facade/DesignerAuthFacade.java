package ditda.backend.domain.designer.auth.facade;

import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.service.EmailVerificationService;
import ditda.backend.domain.common.user.service.UserService;
import ditda.backend.domain.designer.auth.dto.DesignerAuthResult;
import ditda.backend.domain.designer.auth.dto.request.DesignerSignupRequest;
import ditda.backend.domain.designer.auth.dto.request.PortfolioPresignRequest;
import ditda.backend.domain.designer.auth.dto.response.PortfolioPresignResponse;
import ditda.backend.domain.designer.auth.service.DesignerAuthService;
import ditda.backend.domain.designer.auth.service.PortfolioService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerAuthFacade {

	private final DesignerAuthService designerAuthService;
	private final EmailVerificationService emailVerificationService;
	private final PortfolioService portfolioService;
	private final UserService userService;

	public PortfolioPresignResponse issuePortfolioPresignedUrl(PortfolioPresignRequest request) {

		// 이메일 인증 여부 확인
		emailVerificationService.validateVerified(request.email());

		return portfolioService.generatePresignedUpload(request.contentType());
	}

	public DesignerAuthResult signup(DesignerSignupRequest request) {

		// 이메일 인증 여부 확인
		emailVerificationService.validateVerified(request.email());

		// 아이디 및 이메일 검증
		userService.validateUsernameAvailable(request.username());
		userService.validateEmailAvailable(request.email());

		// 포트폴리오 key 검증
		List<String> tempKeys = request.portfolioKeys() == null ? List.of() : request.portfolioKeys();
		portfolioService.validateKeys(tempKeys);

		// 임시('portfolio/tmp') -> 정식('portfolio/') 승격
		List<String> portfolioKeys = portfolioService.promote(tempKeys);

		// 회원가입
		DesignerAuthResult result;
		try {
			result = designerAuthService.signup(request, portfolioKeys);
		} catch (Exception e) {
			portfolioService.deleteFiles(portfolioKeys);
			throw e;
		}

		// 이메일 인증 마크 정리
		emailVerificationService.deleteVerified(request.email());

		return result;
	}
}
