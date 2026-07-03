package ditda.backend.domain.admin.designer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.admin.designer.dto.response.DesignerAccountResponse;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDesignerService {

	private final DesignerService designerService;

	// 디자이너 계좌 정보 조회
	public DesignerAccountResponse getDesignerAccount(Long adminId, Long designerId) {

		// 디자이너 조회
		Designer designer = designerService.getById(designerId);

		log.info("[Admin Audit] adminId={} accessed designerId={} account info", adminId, designerId);

		return DesignerAccountResponse.from(designer);
	}
}
