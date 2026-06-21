package ditda.backend.global.s3.dto;

public record PresignedUpload(String key, String presignedUrl) {
}
