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

		try {
			acquired = rlock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
				distributedLock.timeUnit());

			if (!acquired) {
				log.warn("분산 락 획득 실패. key={}, waitTime={} {}",
					key, distributedLock.waitTime(), distributedLock.timeUnit());
				throw new GeneralException(GeneralErrorCode.LOCK_ACQUISITION_FAILED);
			}

			log.debug("분산 락 획득. key={}", key);
			return aopForTransaction.proceed(joinPoint);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("분산 락 대기 중 interrupt 발생. key={}", key, e);

			throw new GeneralException(GeneralErrorCode.LOCK_ACQUISITION_FAILED);
		} finally {
			if (acquired) {
				if (rlock.isHeldByCurrentThread()) {
					rlock.unlock();
					log.debug("분산 락 해제. key={}", key);
				} else {
					log.warn("분산 락 leaseTime 초과로 이미 해제됨. key={}, waitTime={}, leaseTime={}",
						key, distributedLock.waitTime(), distributedLock.leaseTime());
				}
			}
		}
	}
}
