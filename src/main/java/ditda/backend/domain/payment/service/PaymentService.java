package ditda.backend.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.payment.repository.PaymentRepository;
import ditda.backend.domain.term.entity.PaymentTerm;
import ditda.backend.domain.term.repository.PaymentTermRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentTermRepository paymentTermRepository;

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
}
