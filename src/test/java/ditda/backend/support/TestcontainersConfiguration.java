package ditda.backend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		MySQLContainer container = new MySQLContainer("mysql:8.4")
			.withDatabaseName("ditda");

		container.setCommand(
			"--character-set-server=utf8mb4",
			"--collation-server=utf8mb4_unicode_ci"
		);
		return container;
	}
}
