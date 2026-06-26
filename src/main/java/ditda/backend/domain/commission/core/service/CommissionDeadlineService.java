package ditda.backend.domain.commission.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.processor.ApplicationDeadlineProcessor;
import ditda.backend.domain.commission.core.processor.FirstDraftDeadlineProcessor;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionDeadlineService {

	private static final int MAIL_DISPATCH_HOUR = 9;
	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final CommissionRepository commissionRepository;
	private final ApplicationDeadlineProcessor applicationDeadlineProcessor;
	private final FirstDraftDeadlineProcessor firstDraftDeadlineProcessor;

	public void processApplicationDeadlines() {

		LocalDate today = LocalDate.now(ZONE_KST);

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = today.atTime(MAIL_DISPATCH_HOUR, 0);

		// 모집기간이 지난 외주 조회
		List<Commission> commissions = commissionRepository.findByStatusAndApplicationDeadlineBefore(
			CommissionStatus.RECRUITING,
			today
		);

		for (Commission commission : commissions) {
			try {
				applicationDeadlineProcessor.process(commission.getId(), mailScheduledAt);
			} catch (Exception e) {
				log.error("외주 지원 마감 처리 중 오류 발생. commissionId={}", commission.getId(), e);

				// TODO: 디스코드 웹훅
			}
		}
	}

	public void processFirstDraftDeadlines() {

		LocalDate today = LocalDate.now(ZONE_KST);

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = today.atTime(MAIL_DISPATCH_HOUR, 0);

		// 1차 시안 마감일이 지난 외주 조회
		List<Commission> commissions = commissionRepository.findByStatusAndFirstDraftDeadlineBefore(
			CommissionStatus.DRAFT_SUBMITTING,
			today
		);

		for (Commission commission : commissions) {
			try {
				firstDraftDeadlineProcessor.process(commission.getId(), mailScheduledAt);
			} catch (Exception e) {
				log.error("외주 1차 시안 마감 처리 중 오류 발생. commissionId={}", commission.getId(), e);

				// TODO: 디스코드 웹훅
			}
		}
	}
}
