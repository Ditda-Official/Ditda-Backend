package ditda.backend.domain.commission.core.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.CommissionDetail;
import ditda.backend.domain.commission.core.dto.PriceDetail;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse;
import ditda.backend.domain.commission.core.mapper.CommissionDetailMapper;
import ditda.backend.domain.commission.core.policy.CommissionPricePolicy;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.service.DesignerService;
import ditda.backend.domain.user.entity.User;
import ditda.backend.domain.user.entity.enums.UserRole;
import ditda.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommissionFacade {

	private final CommissionService commissionService;
	private final UserService userService;
	private final DesignerService designerService;
	private final CommissionPricePolicy commissionPricePolicy;
	private final CommissionDetailMapper commissionDetailMapper;

	// 외주 상세 정보 조회
	public CommissionDetailResponse getCommissionDetail(Long userId, Long commissionId) {

		// 상세 정보 조회
		CommissionDetail detail = commissionService.getDetail(commissionId);

		// Designer이면 가격 정보 표시
		User user = userService.findById(userId);

		PriceDetail priceDetail = null;
		if (user.getRole() == UserRole.DESIGNER) {
			Designer designer = designerService.findById(userId);
			priceDetail = commissionPricePolicy.getPriceDetail(
				detail.commission().getCategoryType(),
				designer.getLevel()
			);
		}

		return commissionDetailMapper.toResponse(detail, priceDetail);
	}
}
