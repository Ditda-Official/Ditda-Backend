package ditda.backend.domain.admin.designer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.admin.designer.dto.response.DesignerAccountResponse;
import ditda.backend.domain.admin.designer.dto.response.DesignerPortfolioResponse;
import ditda.backend.domain.admin.designer.mapper.AdminDesignerMapper;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import ditda.backend.domain.designer.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDesignerService {

	private final DesignerService designerService;
	private final PortfolioService portfolioService;
	private final AdminDesignerMapper adminDesignerMapper;

	// 디자이너 계좌 정보 조회
	public DesignerAccountResponse getDesignerAccount(Long adminId, Long designerId) {

		// 디자이너 조회
		Designer designer = designerService.getById(designerId);

		log.info("[Admin Audit] adminId={} accessed designerId={} account info", adminId, designerId);

		return DesignerAccountResponse.from(designer);
	}

	// 디자이너 포트폴리오 조회
	public DesignerPortfolioResponse getDesignerPortfolios(Long adminId, Long designerId) {

		// 디자이너 존재 검증
		designerService.validateExists(designerId);

		// 포트폴리오 key 조회
		List<String> portfolioKeys = portfolioService.getPortfolioKeys(designerId);

		log.info("[Admin Audit] adminId={} accessed designerId={} portfolios (count={})",
			adminId, designerId, portfolioKeys.size());

		return adminDesignerMapper.toPortfolioResponse(designerId, portfolioKeys);
	}
}
