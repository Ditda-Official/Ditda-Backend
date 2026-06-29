package ditda.backend.domain.commission.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.CommissionDetail;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionColor;
import ditda.backend.domain.commission.core.entity.CommissionConcept;
import ditda.backend.domain.commission.core.entity.CommissionFile;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.handler.CategoryDetail;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandlerResolver;
import ditda.backend.domain.commission.core.repository.CommissionColorRepository;
import ditda.backend.domain.commission.core.repository.CommissionConceptRepository;
import ditda.backend.domain.commission.core.repository.CommissionFileRepository;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommissionService {

	private final CommissionRepository commissionRepository;
	private final CommissionConceptRepository commissionConceptRepository;
	private final CommissionColorRepository commissionColorRepository;
	private final CommissionFileRepository commissionFileRepository;
	private final CommissionCategoryHandlerResolver handlerResolver;

	public CommissionDetail getDetail(Long commissionId) {

		// 외주 조회
		Commission commission = commissionRepository.findById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		// 상세 정보 조회
		List<CommissionConcept> concepts = commissionConceptRepository.findByCommissionId(commissionId);
		List<CommissionColor> colors = commissionColorRepository.findByCommissionId(commissionId);
		List<CommissionFile> files = commissionFileRepository.findByCommissionId(commissionId);

		// 카테고리 상세 정보 조회
		CommissionCategoryHandler handler = handlerResolver.resolve(commission.getCategoryType());
		CategoryDetail categoryDetail = handler.loadDetail(commissionId);

		return new CommissionDetail(commission, concepts, colors, files, categoryDetail);
	}
}
