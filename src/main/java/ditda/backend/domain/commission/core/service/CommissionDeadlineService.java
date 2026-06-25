package ditda.backend.domain.commission.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionDeadlineService {

	private static final int MAIL_DISPATCH_HOUR = 9;

	private final CommissionRepository commissionRepository;
	private final CommissionDeadlineProcessor commissionDeadlineProcessor;

	public void processApplicationDeadlines() {

		LocalDate today = LocalDate.now();

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = today.atTime(MAIL_DISPATCH_HOUR, 0);

		// 모집기간이 지난 외주 조회
		List<Commission> commissions = commissionRepository.findByStatusAndApplicationDeadlineBefore(
			CommissionStatus.RECRUITING,
			today
		);

		for (Commission commission : commissions) {
			try {
				commissionDeadlineProcessor.processApplicationDeadline(commission.getId(), mailScheduledAt);
			} catch (Exception e) {
				log.error("외주 지원 마감 처리 중 오류 발생. commissionId={}", commission.getId(), e);

				// TODO: 디스코드 웹훅
			}
		}
	}
}
