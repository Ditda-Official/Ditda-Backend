package ditda.backend.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${swagger.server-url}")
	private String serverUrl;

	@Value("${swagger.server-description}")
	private String serverDescription;

	@Bean
	public OpenAPI openApi() {
		String jwtSchemeName = "JWT Authorization";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

		Components components = new Components()
			.addSecuritySchemes(jwtSchemeName, new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("JWT 토큰을 입력해주세요."));

		Server server = new Server()
			.url(serverUrl)
			.description(serverDescription);

		return new OpenAPI()
			.info(apiInfo())
			.addSecurityItem(securityRequirement)
			.components(components)
			.servers(List.of(server));
	}

	private Info apiInfo() {
		return new Info()
			.title("Ditda API Documentation")
			.description("Ditda Swagger 문서입니다.")
			.version("1.0.0");
	}

}
