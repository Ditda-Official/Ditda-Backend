package ditda.backend.domain.auth.dto;

import ditda.backend.domain.user.entity.enums.UserRole;

public record AuthResult(

	Long userId,

	UserRole userRole,

	String name,

	String profileImage,

	String accessToken,

	String refreshToken
) {
}
