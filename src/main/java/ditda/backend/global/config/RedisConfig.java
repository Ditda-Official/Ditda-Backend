package ditda.backend.global.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.EqualJitterDelay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
			.setAddress("redis://" + host + ":" + port)
			.setConnectTimeout(1000)
			.setTimeout(1000)
			.setRetryAttempts(3)
			.setRetryDelay(new EqualJitterDelay(Duration.ofMillis(200), Duration.ofMillis(500)));

		if (StringUtils.hasText(password)) {
			config.setPassword(password);

		}

		return Redisson.create(config);
	}
}
