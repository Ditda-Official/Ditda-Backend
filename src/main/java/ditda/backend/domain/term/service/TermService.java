package ditda.backend.domain.term.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.payment.entity.Payment;
import ditda.backend.domain.term.dto.TermAgreement;
import ditda.backend.domain.term.entity.PaymentTerm;
import ditda.backend.domain.term.entity.UserTerm;
import ditda.backend.domain.term.entity.enums.TermType;
import ditda.backend.domain.term.exception.TermErrorCode;
import ditda.backend.domain.term.repository.PaymentTermRepository;
import ditda.backend.domain.term.repository.UserTermRepository;
import ditda.backend.domain.user.entity.User;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermService {

	private static final Set<TermType> DESIGNER_REQUIRED = Set.of(
		TermType.DESIGNER_SERVICE,
		TermType.USERINFO
	);
	private static final Set<TermType> INSTRUCTOR_REQUIRED = Set.of(
		TermType.INSTRUCTOR_SERVICE,
		TermType.USERINFO
	);

	private final UserTermRepository userTermRepository;
	private final PaymentTermRepository paymentTermRepository;

	@Transactional
	public void savePaymentTerm(Payment payment, String version, boolean isAgreed) {

		PaymentTerm paymentTerm = PaymentTerm.create(payment, version, isAgreed);
		paymentTermRepository.save(paymentTerm);
	}

	@Transactional
	public void saveDesignerTerms(User user, List<TermAgreement> agreements) {
		validateRequiredAgreements(agreements, DESIGNER_REQUIRED);
		saveAllTerms(user, agreements);
	}

	@Transactional
	public void saveInstructorTerms(User user, List<TermAgreement> agreements) {
		validateRequiredAgreements(agreements, INSTRUCTOR_REQUIRED);
		saveAllTerms(user, agreements);
	}

	private void validateRequiredAgreements(List<TermAgreement> agreements, Set<TermType> required) {
		for (TermType type : required) {
			boolean agreed = agreements.stream()
				.anyMatch(a -> a.type() == type && a.isAgreed());
			if (!agreed) {
				throw new GeneralException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED);
			}
		}
	}

	private void saveAllTerms(User user, List<TermAgreement> agreements) {
		List<UserTerm> terms = agreements.stream()
			.map(a -> UserTerm.createTerm(user, a.type(), a.version(), a.isAgreed()))
			.toList();

		userTermRepository.saveAll(terms);
	}
}

