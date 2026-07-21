package ditda.backend.global.config;

import org.springframework.context.annotation.Configuration;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import jakarta.annotation.PostConstruct;

@Configuration
public class LoggingConfig {

	@PostConstruct
	void registerMdcAccessor() {
		ContextRegistry.getInstance().registerThreadLocalAccessor(new Slf4jThreadLocalAccessor());
	}
}
