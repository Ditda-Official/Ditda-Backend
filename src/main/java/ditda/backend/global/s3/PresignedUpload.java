package ditda.backend.global.s3;

public record PresignedUpload(String key, String presignedUrl) {
}
