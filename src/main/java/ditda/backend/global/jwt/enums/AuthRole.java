package ditda.backend.global.jwt.enums;

import ditda.backend.domain.user.entity.enums.UserRole;

public enum AuthRole {

	DESIGNER,
	INSTRUCTOR,
	ADMIN;

	public static AuthRole from(UserRole userRole) {
		return switch (userRole) {
			case DESIGNER -> DESIGNER;
			case INSTRUCTOR -> INSTRUCTOR;
		};
	}
}
