package ditda.backend.global.jwt.dto;

import ditda.backend.domain.user.entity.enums.UserRole;

public record AccessTokenPayload(

	Long userId,

	UserRole role
) {
}
