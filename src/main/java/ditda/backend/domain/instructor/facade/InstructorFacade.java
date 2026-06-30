package ditda.backend.domain.instructor.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.instructor.dto.response.InstructorStatsResponse;
import ditda.backend.domain.instructor.entity.Instructor;
import ditda.backend.domain.instructor.mapper.InstructorResponseMapper;
import ditda.backend.domain.instructor.service.InstructorService;
import ditda.backend.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorFacade {

	private final InstructorService instructorService;
	private final InstructorCommissionService instructorCommissionService;
	private final PaymentService paymentService;
	private final InstructorResponseMapper instructorResponseMapper;

	// 강사 통계 조회
	@Transactional(readOnly = true)
	public InstructorStatsResponse getMyStats(Long instructorId) {

		Instructor instructor = instructorService.getByIdWithUser(instructorId);

		// 외주 이용 횟수 = 외주 결제 완료 (PaymentStatus = COMPLETED) 된 건수
		long total = paymentService.countPaidCommissions(instructorId);
		// 진행 중인 외주 건수 = 외주 Status가 모집(RECRUITING), 시안 제출(DRAFT_SUBMITTING), 강사 선택(DRAFT_SELECTING), 수정(EDITING) 건수
		long ongoing = instructorCommissionService.countOngoingCommissions(instructorId);

		return instructorResponseMapper.toInstructorStatsResponse(
			instructor.getName(),
			instructor.getUser().getProfileImage(),
			total,
			ongoing
		);
	}
}
