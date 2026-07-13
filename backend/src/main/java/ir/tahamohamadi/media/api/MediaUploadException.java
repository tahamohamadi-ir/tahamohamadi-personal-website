package ir.tahamohamadi.media.api;

import org.springframework.http.HttpStatus;

public class MediaUploadException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public MediaUploadException(String message) {
        this(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA", message, null);
    }

    public MediaUploadException(String message, Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, "MEDIA_STORAGE_FAILURE", message, cause);
    }

    private MediaUploadException(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public static MediaUploadException invalid(String message) {
        return new MediaUploadException(HttpStatus.BAD_REQUEST, "INVALID_MEDIA", message, null);
    }

    public static MediaUploadException tooLarge(String message) {
        return new MediaUploadException(HttpStatus.PAYLOAD_TOO_LARGE, "MEDIA_TOO_LARGE", message, null);
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}
