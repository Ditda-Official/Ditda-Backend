package ditda.backend.domain.commission.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.DateInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.DesignInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.TermRequest;
import ditda.backend.domain.commission.core.dto.response.CommissionCreateResponse;
import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionColor;
import ditda.backend.domain.commission.core.entity.CommissionConcept;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.repository.CommissionColorRepository;
import ditda.backend.domain.commission.core.repository.CommissionConceptRepository;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.commission.core.vo.CommissionFileToSave;
import ditda.backend.domain.instructor.entity.Instructor;
import ditda.backend.domain.instructor.repository.InstructorRepository;
import ditda.backend.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionService {

	private final CommissionRepository commissionRepository;
	private final CommissionConceptRepository commissionConceptRepository;
	private final CommissionColorRepository commissionColorRepository;
	private final InstructorRepository instructorRepository;
	private final CommissionCreateFileService commissionCreateFileService;
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
		CommissionCategoryHandler handler,
		List<CommissionFileToSave> commissionFiles
	) {

		DesignInfo design = request.designInfo();
		DateInfo date = request.date();
		TermRequest term = request.term();

		// 1. Commission 저장
		Instructor instructor = instructorRepository.getReferenceById(instructorId);
		String title = handler.buildTitle(request);
		Commission commission = Commission.create(
			instructor,
			request.plan(),
			title,
			request.category(),
			design.pageSize(),
			design.additionalConcept(),
			design.colorSelectionMode(),
			date.firstDraftDeadline(),
			date.finalDeadline()
		);
		commissionRepository.save(commission);

		// 2. 공통 섹션 저장
		saveConcepts(commission, design);
		saveColors(commission, design);
		saveFiles(commission, commissionFiles);

		// 3. 카테고리별 전용 섹션 저장
		handler.saveDetail(commission, request);

		// 4. 결제(입금 대기 [PENDING]) + 결제 약관 저장
		String depositorName = instructor.getName();
		paymentService.createPendingPayment(
			commission,
			depositorName,
			term.version(),
			term.isAgreed()
		);

		return CommissionCreateResponse.from(commission);
	}

	// 컨셉 저장
	private void saveConcepts(Commission commission, DesignInfo design) {

		List<CommissionConcept> concepts = design.concepts().stream()
			.map(tag -> CommissionConcept.create(commission, tag))
			.toList();
		commissionConceptRepository.saveAll(concepts);
	}

	// 색상 저장 (강사 직접 선택시)
	private void saveColors(Commission commission, DesignInfo design) {

		if (design.colorSelectionMode() != ColorSelectionMode.USER_SELECTED) {
			return;
		}

		List<CommissionColor> colors = design.colors().stream()
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
