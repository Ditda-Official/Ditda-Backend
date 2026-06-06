package ditda.backend.domain.payment.service;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.payment.dto.response.DepositNotifyResponse;
import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.payment.entity.enums.PaymentStatus;
import ditda.backend.domain.payment.event.DepositNotifiedEvent;
import ditda.backend.domain.payment.exception.PaymenErrorCode;
import ditda.backend.domain.payment.repository.PaymentRepository;
import ditda.backend.domain.term.service.TermService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final TermService termService;

	// 강사 새 외주 작성시 결제 대기 상태 생성
	@Transactional
	public void createPendingPayment(
		Commission commission,
		String depositorName,
		String termVersion,
		boolean isTermAgreed
	) {

		Payment payment = Payment.create(
			commission,
			depositorName
		);
		paymentRepository.save(payment);

		termService.savePaymentTerm(payment, termVersion, isTermAgreed);
	}

	@Transactional
	public DepositNotifyResponse notifyDeposit(Long instructorId, Long commissionId) {

		Payment payment = paymentRepository.findByCommissionId(commissionId)
			.orElseThrow(() -> new GeneralException(PaymenErrorCode.PAYMENT_NOT_FOUND));

		Commission commission = payment.getCommission();

		if (!Objects.equals(commission.getInstructor().getId(), instructorId)) {
			throw new GeneralException(PaymenErrorCode.PAYMENT_ACCESS_DENIED);
		}

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new GeneralException(PaymenErrorCode.DEPOSIT_NOTIFY_NOT_ALLOWED);
		}

		if (payment.getDepositNotifiedAt() != null) {
			throw new GeneralException(PaymenErrorCode.DEPOSIT_NOTIFY_NOT_ALLOWED);
		}

		payment.markDepositNotified();

		eventPublisher.publishEvent(new DepositNotifiedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getName(),
			payment.getDepositorName(),
			payment.getAmount(),
			payment.getDepositNotifiedAt()
		));

		return DepositNotifyResponse.of(
			commission.getId(),
			payment.getStatus(),
			payment.getDepositNotifiedAt()
		);
	}
}
