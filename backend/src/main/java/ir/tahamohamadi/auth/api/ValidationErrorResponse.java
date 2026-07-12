package ir.tahamohamadi.auth.api;

import java.util.List;

public record ValidationErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationFieldError> validationErrors
) {

    public ValidationErrorResponse {
        validationErrors = List.copyOf(validationErrors);
    }
}
