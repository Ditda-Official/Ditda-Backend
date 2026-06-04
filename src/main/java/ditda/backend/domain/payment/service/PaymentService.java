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
import ditda.backend.domain.payment.exception.PaymentException;
import ditda.backend.domain.payment.repository.PaymentRepository;
import ditda.backend.domain.term.entity.PaymentTerm;
import ditda.backend.domain.term.repository.PaymentTermRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentTermRepository paymentTermRepository;
	private final ApplicationEventPublisher eventPublisher;

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

		PaymentTerm paymentTerm = PaymentTerm.create(
			payment,
			termVersion,
			isTermAgreed
		);
		paymentTermRepository.save(paymentTerm);
	}

	@Transactional
	public DepositNotifyResponse notifyDeposit(Long instructorId, Long commissionId) {

		Payment payment = paymentRepository.findByCommissionId(commissionId)
			.orElseThrow(() -> new GeneralException(PaymentException.PAYMENT_NOT_FOUND));

		Commission commission = payment.getCommission();

		if (!Objects.equals(commission.getInstructor().getId(), instructorId)) {
			throw new GeneralException(PaymentException.PAYMENT_ACCESS_DENIED);
		}

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new GeneralException(PaymentException.DEPOSIT_NOTIFY_NOT_ALLOWED);
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
