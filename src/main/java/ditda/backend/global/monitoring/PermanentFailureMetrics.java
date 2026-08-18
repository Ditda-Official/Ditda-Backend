package ditda.backend.global.monitoring;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class PermanentFailureMetrics {

	private static final String METRIC_NAME = "ditda.permanent.failure";

	private final Counter watermarkFailure;
	private final Counter notificationFailure;

	public PermanentFailureMetrics(MeterRegistry meterRegistry) {
		watermarkFailure = Counter.builder(METRIC_NAME)
			.tag("kind", "watermark")
			.description("재시도 소진으로 워터마크가 영구 실패한 시안 파일 수")
			.register(meterRegistry);

		notificationFailure = Counter.builder(METRIC_NAME)
			.tag("kind", "notification")
			.description("재시도 소진으로 발송에 영구 실패한 알림 메일 수")
			.register(meterRegistry);
	}

	public void watermarkFailed() {
		watermarkFailure.increment();
	}

	public void watermarkFailed(int count) {
		watermarkFailure.increment(count);
	}

	public void notificationFailed() {
		notificationFailure.increment();
	}
}
