package ditda.backend.global.s3.enums;

import java.util.EnumSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadTarget {

	COMMISSION_MATERIAL("commission/material", BucketType.PRIVATE, EnumSet.of(S3ContentType.PNG)),
	COMMISSION_REFERENCE("commission/reference", BucketType.PRIVATE, EnumSet.of(S3ContentType.PNG)),
	PORTFOLIO("portfolio", BucketType.PRIVATE, EnumSet.of(S3ContentType.PNG, S3ContentType.PDF));

	private final String dir;
	private final BucketType bucketType;
	private final Set<S3ContentType> allowed;
}
