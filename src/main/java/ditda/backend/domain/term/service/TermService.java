package ditda.backend.domain.term.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.term.dto.TermAgreement;
import ditda.backend.domain.term.entity.UserTerm;
import ditda.backend.domain.term.entity.enums.TermType;
import ditda.backend.domain.term.exception.TermErrorCode;
import ditda.backend.domain.term.repository.UserTermRepository;
import ditda.backend.domain.user.entity.User;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermService {

	private static final Set<TermType> REQUIRED_TERMS = Set.of(
		TermType.SERVICE,
		TermType.USERINFO,
		TermType.SETTLEMENT,
		TermType.DISINTERMEDIATION
	);
	private final UserTermRepository userTermRepository;

	@Transactional
	public void saveTerms(User user, List<TermAgreement> agreements) {
		validateRequiredAgreements(agreements);

		List<UserTerm> terms = agreements.stream()
			.map(a -> UserTerm.createTerm(user, a.type(), a.version(), a.isAgreed()))
			.toList();

		userTermRepository.saveAll(terms);
	}

	private void validateRequiredAgreements(List<TermAgreement> agreements) {
		for (TermType required : REQUIRED_TERMS) {
			boolean agreed = agreements.stream()
				.anyMatch(a -> a.type() == required && a.isAgreed());
			if (!agreed) {
				throw new GeneralException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED);
			}
		}
	}
}

