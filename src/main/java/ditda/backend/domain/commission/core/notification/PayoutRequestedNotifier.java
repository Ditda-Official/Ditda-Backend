package ditda.backend.domain.commission.core.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.event.PayoutRequestedEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutRequestedNotifier {

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;

	@EventListener
	public void onPayoutRequested(PayoutRequestedEvent event) {

		int totalAmount = event.payouts().stream()
			.mapToInt(PayoutRequestedEvent.PayoutInfo::amount)
			.sum();

		List<Map<String, Object>> payoutsForTemplate = event.payouts().stream()
			.map(p -> {
				Map<String, Object> payoutMap = new HashMap<>();
				payoutMap.put("designerId", p.designerId());
				payoutMap.put("designerName", p.designerName());
				payoutMap.put("level", p.level().name());
				payoutMap.put("amount", p.amount());
				return payoutMap;
			})
			.toList();

		Map<String, Object> variables = new HashMap<>();
		variables.put("commissionId", event.commissionId());
		variables.put("commissionTitle", event.commissionTitle());
		variables.put("instructorName", event.instructorName());
		variables.put("instructorEmail", event.instructorEmail());
		variables.put("reason", event.reason().name());
		variables.put("payouts", payoutsForTemplate);
		variables.put("payoutCount", payoutsForTemplate.size());
		variables.put("totalAmount", totalAmount);

		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			buildSubject(event.reason()),
			"email/admin-payout-request",
			variables,
			event.mailScheduledAt()
		));
	}

	private String buildSubject(PayoutRequestedEvent.PayoutReason reason) {
		return switch (reason) {
			case FINAL_COMPLETED_AUTO, FINAL_COMPLETED_MANUAL -> "[DITDA] 외주 최종 확정 - 디자이너 정산 요청";
			case FINAL_CANCELLED_BY_DEADLINE -> "[DITDA] 외주 취소 - 제출 디자이너 정산 요청";
			case DRAFT_SELECTION_REJECTED -> "[DITDA] 시안 선택 완료 - 미선택 디자이너 정산 요청";
		};
	}
}
