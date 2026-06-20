package ditda.backend.domain.commission.revision.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.service.DraftService;
import ditda.backend.domain.commission.revision.dto.response.InstructorRevisionDetailResponse;
import ditda.backend.domain.commission.revision.exception.RevisionErrorCode;
import ditda.backend.domain.commission.revision.mapper.RevisionMapper;
import ditda.backend.domain.commission.revision.service.RevisionService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorRevisionFacade {

	private final InstructorCommissionService instructorCommissionService;
	private final RevisionService revisionService;
	private final DraftService draftService;
	private final RevisionMapper revisionMapper;

	public InstructorRevisionDetailResponse getRevisionDetail(Long instructorId, Long commissionId) {

		// 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 수정 단계인지 검증
		if (!commission.isRevisable()) {
			throw new GeneralException(RevisionErrorCode.COMMISSION_NOT_REVISABLE);
		}

		// 선택된 디자이너의 가장 최근 시안
		CommissionDraft latestDraft = draftService.getLatestDraftOfSelectedApplication(commissionId);

		// 시안에 달린 디자이너 코멘트
		String designerComment = revisionService.getDesignerComment(latestDraft.getId());

		// 썸네일
		CommissionDraftFile thumbnail = draftService.getThumbnail(latestDraft.getId());

		// 현재 수정 차수
		int currentRevisionCount = revisionService.calculateCurrentRevisionCount(commission);

		return revisionMapper.toInstructorRevisionDetailResponse(
			commission,
			latestDraft,
			thumbnail,
			designerComment,
			currentRevisionCount
		);

	}
}
