package ditda.backend.global.config;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	private final MdcTaskDecorator mdcTaskDecorator = new MdcTaskDecorator();

	@Bean(name = "watermarkExecutor")
	public Executor watermarkExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("async-watermark-");

		executor.setTaskDecorator(mdcTaskDecorator);    // MDC 전파

		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);

		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) ->
			log.error("Async task error. method={}", method.getName(), ex);
	}

	// 부모 스레드의 MDC 맵을 복사하여 자식 스레드 생성 시 적용
	private static class MdcTaskDecorator implements TaskDecorator {
		@Override
		public Runnable decorate(Runnable runnable) {
			Map<String, String> contextMap = MDC.getCopyOfContextMap();
			return () -> {
				try {
					if (contextMap != null) {
						MDC.setContextMap(contextMap);
					}
					runnable.run();
				} finally {
					MDC.clear();
				}
			};
		}
	}
}
