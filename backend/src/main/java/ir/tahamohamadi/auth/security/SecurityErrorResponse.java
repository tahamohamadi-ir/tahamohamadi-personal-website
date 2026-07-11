package ir.tahamohamadi.auth.security;

public record SecurityErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
