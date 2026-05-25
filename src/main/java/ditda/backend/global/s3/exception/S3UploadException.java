package ditda.backend.global.s3.exception;

public class S3UploadException extends RuntimeException {

	public S3UploadException(String filename, Throwable cause) {
		super("Failed to upload file to S3: " + filename, cause);
	}
}
