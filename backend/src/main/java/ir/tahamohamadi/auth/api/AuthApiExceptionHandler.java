package ir.tahamohamadi.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice(assignableTypes = AuthSessionController.class)
public class AuthApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ValidationFieldError> validationErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationFieldError)
                .toList();
        return badRequest(request, validationErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ValidationErrorResponse> handleUnreadableRequest(HttpServletRequest request) {
        return badRequest(request, List.of(new ValidationFieldError("request", "Invalid request body")));
    }

    private ResponseEntity<ValidationErrorResponse> badRequest(
            HttpServletRequest request,
            List<ValidationFieldError> validationErrors
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new ValidationErrorResponse(
                        Instant.now().toString(),
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        "Invalid login request",
                        request.getRequestURI(),
                        validationErrors
                ));
    }

    private ValidationFieldError toValidationFieldError(FieldError fieldError) {
        return new ValidationFieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
