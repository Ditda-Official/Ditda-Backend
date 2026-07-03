package ditda.backend.domain.admin.auth.dto.response;

import ditda.backend.domain.admin.core.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어드민 로그인 응답")
public record AdminLoginResponse(

	@Schema(description = "로그인 유저 ID", example = "1")
	Long adminId,

	@Schema(description = "어드민 이름", example = "홍길동")
	String name,

	@Schema(description = "Access Token")
	String accessToken
) {

	public static AdminLoginResponse of(Admin admin, String accessToken) {
		return new AdminLoginResponse(admin.getId(), admin.getName(), accessToken);
	}
}
