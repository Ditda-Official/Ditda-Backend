package ditda.backend.global.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

	private static final String LOCK_PREFIX = "lock:";

	private final RedissonClient redissonClient;
	private final AopForTransaction aopForTransaction;

	@Around("@annotation(ditda.backend.global.lock.DistributedLock)")
	public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);

		String key = LOCK_PREFIX + CustomSpringElParser.getDynamicValue(
			signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

		RLock rlock = redissonClient.getLock(key);
		boolean acquired = false;
		long acquiredAt = 0L;

		try {
			acquired = rlock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
				distributedLock.timeUnit());

			if (!acquired) {
				log.warn("Failed to acquire distributed lock. key={}, waitTimeMs={}",
					key, distributedLock.timeUnit().toMillis(distributedLock.waitTime()));
				throw new GeneralException(GeneralErrorCode.LOCK_ACQUISITION_FAILED);
			}

			acquiredAt = System.nanoTime();

			return aopForTransaction.proceed(joinPoint);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Interrupted while waiting for distributed lock. key={}", key, e);

			throw new GeneralException(GeneralErrorCode.LOCK_ACQUISITION_FAILED);
		} finally {
			if (acquired) {
				try {
					if (rlock.isHeldByCurrentThread()) {
						rlock.unlock();
					} else {
						log.warn("Distributed lock lease expired before release. key={}, leaseTimeMs={}, elapsedMs={}",
							key, distributedLock.timeUnit().toMillis(distributedLock.leaseTime()),
							elapsedMs(acquiredAt));
					}
				} catch (Exception exception) {
					log.warn("Failed to release distributed lock. key={}", key, exception);
				}
			}
		}
	}

	private long elapsedMs(long acquiredAt) {
		return (System.nanoTime() - acquiredAt) / 1_000_000;
	}
}
