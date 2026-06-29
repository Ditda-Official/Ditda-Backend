package ditda.backend.domain.commission.core.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.CommissionDetail;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse.DateInfo;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse.DesignInfo;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse.FileInfo;
import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse.PriceInfo;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionColor;
import ditda.backend.domain.commission.core.entity.CommissionConcept;
import ditda.backend.domain.commission.core.entity.CommissionFile;
import ditda.backend.domain.commission.core.entity.enums.ConceptTag;
import ditda.backend.domain.commission.core.entity.enums.FileKind;
import ditda.backend.global.s3.manager.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionDetailMapper {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	public CommissionDetailResponse toResponse(CommissionDetail detail, PriceInfo priceInfo) {
		Commission commission = detail.commission();

		return new CommissionDetailResponse(
			commission.getId(),
			commission.getTitle(),
			commission.getCategoryType(),
			commission.getStatus(),
			toDesignInfo(commission, detail.concepts(), detail.colors()),
			detail.categoryDetail().toResponse(),
			toFileInfos(detail.files()),
			toDateInfo(commission),
			priceInfo
		);
	}

	private DesignInfo toDesignInfo(
		Commission commission,
		List<CommissionConcept> concepts,
		List<CommissionColor> colors
	) {

		List<ConceptTag> conceptTags = concepts.stream()
			.map(CommissionConcept::getConcept)
			.toList();

		List<DesignInfo.ColorInfo> colorInfos = colors.stream()
			.map(c -> new DesignInfo.ColorInfo(c.getRole(), c.getColorCode()))
			.toList();

		return new DesignInfo(
			commission.getPageSize(),
			conceptTags,
			commission.getAdditionalConcept(),
			commission.getColorSelectionMode(),
			colorInfos
		);
	}

	private List<FileInfo> toFileInfos(List<CommissionFile> files) {

		Map<FileKind, List<CommissionFile>> groupedFiles = files.stream()
			.collect(Collectors.groupingBy(CommissionFile::getFileKind));

		return groupedFiles.entrySet().stream()
			.map(e -> {
				List<String> urls = e.getValue().stream()
					.map(this::resolveUrl)
					.toList();

				String description = e.getValue().getFirst().getDescription();
				return new FileInfo(e.getKey(), urls, description);
			})
			.toList();
	}

	private DateInfo toDateInfo(Commission commission) {
		return new DateInfo(
			commission.getApplicationDeadline(),
			commission.getFirstDraftDeadline(),
			commission.getFinalDeadline()
		);
	}

	private String resolveUrl(CommissionFile file) {
		return s3PresignedUrlGenerator.generatePrivateGetUrl(file.getFileUrl());
	}
}
