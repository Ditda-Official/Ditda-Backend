package ditda.backend.domain.commission.core.service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.ColorInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.DateInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.TermRequest;
import ditda.backend.domain.commission.core.dto.response.CommissionCreateResponse;
import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionColor;
import ditda.backend.domain.commission.core.entity.CommissionConcept;
import ditda.backend.domain.commission.core.entity.enums.ColorRole;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandlerResolver;
import ditda.backend.domain.commission.core.repository.CommissionColorRepository;
import ditda.backend.domain.commission.core.repository.CommissionConceptRepository;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.commission.core.vo.CommissionFileToSave;
import ditda.backend.domain.instructor.entity.Instructor;
import ditda.backend.domain.instructor.repository.InstructorRepository;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.domain.term.entity.enums.TermType;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionService {

	private static final int MIN_FIRST_DRAFT_LEAD_DAYS = 10;
	private static final int MIN_DRAFT_TO_FINAL_DAYS = 14;

	private final CommissionRepository commissionRepository;
	private final CommissionConceptRepository commissionConceptRepository;
	private final CommissionColorRepository commissionColorRepository;
	private final InstructorRepository instructorRepository;
	private final CommissionCreateFileService commissionCreateFileService;
	private final CommissionCategoryHandlerResolver commissionCategoryHandlerResolver;
	private final PaymentService paymentService;

	// 외주 플랜 정보 조회
	@Transactional(readOnly = true)
	public PlanListResponse getPlans() {
		return PlanListResponse.from();
	}

	// 새 외주 생성
	@Transactional
	public CommissionCreateResponse createCommission(
		Long instructorId,
		CommissionCreateRequest request,
		List<CommissionFileToSave> commissionFiles
	) {

		// 1. 공통 검증
		validateColors(request);
		validateTerm(request);
		validateDeadlines(request);

		// 2. 카테고리 핸들러 + 전용 검증
		CommissionCategoryHandler handler = commissionCategoryHandlerResolver.resolve(request.category());
		handler.validate(request);

		// 3. Commission 저장
		Instructor instructor = instructorRepository.getReferenceById(instructorId);
		String title = handler.buildTitle(request);
		Commission commission = Commission.create(
			instructor,
			request.plan(),
			title,
			request.category(),
			request.designInfo().pageSize(),
			request.designInfo().additionalConcept(),
			request.designInfo().colorSelectionMode(),
			request.date().firstDraftDeadline(),
			request.date().finalDeadline()
		);
		commissionRepository.save(commission);

		// 4. 공통 섹션 저장
		saveConcepts(commission, request);
		saveColors(commission, request);
		saveFiles(commission, commissionFiles);

		// 5. 카테고리별 전용 섹션 저장
		handler.saveDetail(commission, request);

		// 6. 결제(입금 대기 [PENDING]) + 결제 약관 저장
		String depositorName = instructor.getUser().getName();
		paymentService.createPendingPayment(
			commission,
			depositorName,
			request.term().version(),
			request.term().isAgreed()
		);

		return CommissionCreateResponse.from(commission);
	}

	// 색상 검증 (직접 색상 지정시 MAIN, SUB1, SUB2 필수)
	private void validateColors(CommissionCreateRequest request) {

		if (request.designInfo().colorSelectionMode() != ColorSelectionMode.USER_SELECTED) {
			return;
		}

		List<ColorInfo> colors = request.designInfo().colors();
		if (colors == null || colors.isEmpty()) {
			throw new GeneralException(CommissionErrorCode.COLORS_REQUIRED);
		}

		Set<ColorRole> roles = colors.stream()
			.map(ColorInfo::role)
			.collect(Collectors.toSet());

		if (roles.size() != colors.size() || !roles.equals(EnumSet.allOf(ColorRole.class))) {
			throw new GeneralException(CommissionErrorCode.COLORS_REQUIRED);
		}
	}

	// 결제 약관 검증
	private void validateTerm(CommissionCreateRequest request) {

		TermRequest term = request.term();
		if (term.type() != TermType.SETTLEMENT || !Boolean.TRUE.equals(term.isAgreed())) {
			throw new GeneralException(CommissionErrorCode.SETTLEMENT_TERM_NOT_AGREED);
		}
	}

	// 마감 기한 검증
	private void validateDeadlines(CommissionCreateRequest request) {

		DateInfo date = request.date();
		LocalDate today = LocalDate.now();

		if (!date.firstDraftDeadline().isBefore(date.finalDeadline())) {
			throw new GeneralException(CommissionErrorCode.INVALID_DEADLINE_ORDER);
		}

		if (date.firstDraftDeadline().minusDays(MIN_FIRST_DRAFT_LEAD_DAYS).isBefore(today)) {
			throw new GeneralException(CommissionErrorCode.FIRST_DRAFT_DEADLINE_TOO_SOON);
		}

		if (date.finalDeadline().isBefore(date.firstDraftDeadline().plusDays(MIN_DRAFT_TO_FINAL_DAYS))) {
			throw new GeneralException(CommissionErrorCode.FINAL_DEADLINE_TOO_SOON);
		}
	}

	// 컨셉 저장
	private void saveConcepts(Commission commission, CommissionCreateRequest request) {

		List<CommissionConcept> concepts = request.designInfo().concepts().stream()
			.map(tag -> CommissionConcept.create(commission, tag))
			.toList();
		commissionConceptRepository.saveAll(concepts);
	}

	// 색상 저장 (강사 직접 선택시)
	private void saveColors(Commission commission, CommissionCreateRequest request) {

		if (request.designInfo().colorSelectionMode() != ColorSelectionMode.USER_SELECTED) {
			return;
		}

		List<CommissionColor> colors = request.designInfo().colors().stream()
			.map(color -> CommissionColor.create(commission, color.role(), color.colorCode()))
			.toList();
		commissionColorRepository.saveAll(colors);
	}

	// 첨부 파일 저장 (자료첨부, 레퍼런스)
	private void saveFiles(Commission commission, List<CommissionFileToSave> commissionFiles) {

		for (CommissionFileToSave file : commissionFiles) {
			commissionCreateFileService.saveCommissionFiles(
				commission,
				file.fileKind(),
				file.keys(),
				file.description()
			);
		}
	}
}
