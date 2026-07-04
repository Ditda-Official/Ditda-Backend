package ditda.backend.global.jwt.dto;

import ditda.backend.global.jwt.enums.AuthRole;

public record AccessTokenPayload(

	Long userId,

	AuthRole role
) {
}
