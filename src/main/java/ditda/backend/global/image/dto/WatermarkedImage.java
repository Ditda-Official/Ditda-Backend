package ditda.backend.global.image.dto;

import ditda.backend.global.s3.enums.S3ContentType;

public record WatermarkedImage(byte[] bytes, S3ContentType contentType) {
}
