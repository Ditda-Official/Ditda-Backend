package ditda.backend.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

	// lock 이름
	String key();

	// lock 시간 단위
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	// lock 대기 시간
	long waitTime() default 5L;

	// lock 임대 시간
	long leaseTime() default 10L;
}
