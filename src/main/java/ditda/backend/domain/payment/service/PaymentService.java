package ditda.backend.domain.payment.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.payment.dto.response.DepositNotifyResponse;
import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.payment.entity.enums.PaymentStatus;
import ditda.backend.domain.payment.event.DepositNotifiedEvent;
import ditda.backend.domain.payment.exception.PaymentErrorCode;
import ditda.backend.domain.payment.repository.PaymentRepository;
import ditda.backend.domain.payment.repository.projection.CommissionPaidAmount;
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
			.orElseThrow(() -> new GeneralException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		Commission commission = payment.getCommission();

		if (!Objects.equals(commission.getInstructor().getId(), instructorId)) {
			throw new GeneralException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
		}

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw new GeneralException(PaymentErrorCode.DEPOSIT_NOTIFY_NOT_ALLOWED);
		}

		if (payment.getDepositNotifiedAt() != null) {
			throw new GeneralException(PaymentErrorCode.DEPOSIT_NOTIFY_NOT_ALLOWED);
		}

		payment.markDepositNotified();

		eventPublisher.publishEvent(new DepositNotifiedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getName(),
			payment.getDepositorName(),
			payment.getAmount(),
			payment.getDepositNotifiedAt(),
			LocalDateTime.now()
		));

		return DepositNotifyResponse.of(
			commission.getId(),
			payment.getStatus(),
			payment.getDepositNotifiedAt()
		);
	}

	// 결제 완료된 외주들의 결제 금액 조회
	@Transactional(readOnly = true)
	public Map<Long, Integer> getPaidAmounts(Collection<Long> commissionIds) {

		if (commissionIds.isEmpty()) {
			return Map.of();
		}

		return paymentRepository.findPaidAmounts(commissionIds, PaymentStatus.COMPLETED).stream()
			.collect(Collectors.toMap(
					CommissionPaidAmount::getCommissionId,
					CommissionPaidAmount::getAmount
				)
			);
	}

	// 결제 완료된 외주 카운트
	@Transactional(readOnly = true)
	public long countPaidCommissions(Long instructorId) {
		return paymentRepository.countByInstructorIdAndStatus(instructorId, PaymentStatus.COMPLETED);
	}

	// 전액 환불
	@Transactional
	public int requestFullRefund(Long commissionId) {

		// 결제 내역 조회
		Payment payment = paymentRepository.findByCommissionId(commissionId)
			.orElseThrow(() -> new GeneralException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		// 전액 환불
		payment.markFullRefundRequested();

		return payment.getAmount();
	}

	// 부분 환불
	@Transactional
	public void requestPartialRefund(Long commissionId, int refundAmount) {

		// 결제 내역 조회
		Payment payment = paymentRepository.findByCommissionId(commissionId)
			.orElseThrow(() -> new GeneralException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		// 부분 환불
		payment.markPartialRefundRequested(refundAmount);
	}
}
