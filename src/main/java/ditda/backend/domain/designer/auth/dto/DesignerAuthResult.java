package ditda.backend.domain.designer.auth.dto;

public record DesignerAuthResult(

	Long userId,

	String name,

	String profileImage,

	String accessToken,

	String refreshToken
) {
}
