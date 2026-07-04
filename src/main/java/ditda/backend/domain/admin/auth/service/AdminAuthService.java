package ditda.backend.domain.admin.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.admin.auth.dto.request.AdminLoginRequest;
import ditda.backend.domain.admin.auth.dto.response.AdminLoginResponse;
import ditda.backend.domain.admin.core.entity.Admin;
import ditda.backend.domain.admin.core.repository.AdminRepository;
import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.enums.AuthRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AdminLoginResponse login(AdminLoginRequest request) {

		// 어드민 조회
		Admin admin = adminRepository.findByUsername(request.username())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.INVALID_LOGIN));

		// 비밀번호 검증
		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new GeneralException(GeneralErrorCode.INVALID_LOGIN);
		}

		// Access Token 발급
		String accessToken = jwtTokenProvider.generateAccessToken(admin.getId(), AuthRole.ADMIN);

		return AdminLoginResponse.of(admin, accessToken);
	}
}
