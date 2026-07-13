package ir.tahamohamadi.common.api;

import ir.tahamohamadi.media.api.MediaUploadException;
import ir.tahamohamadi.blog.post.api.admin.PublishValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiErrorResponse> invalid(MethodArgumentNotValidException e,HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",r,e.getBindingResult().getFieldErrors().stream().map(f->new FieldValidationError(f.getField(),f.getDefaultMessage())).toList()); }
    @ExceptionHandler(MediaUploadException.class) ResponseEntity<ApiErrorResponse> media(MediaUploadException e,HttpServletRequest r) { return response(e.status(),e.code(),e.status().is5xxServerError()?"Unable to process uploaded file":"Uploaded file was rejected",r,List.of()); }
    @ExceptionHandler(NoSuchElementException.class) ResponseEntity<ApiErrorResponse> absent(NoSuchElementException e,HttpServletRequest r) { return response(HttpStatus.NOT_FOUND,"RESOURCE_NOT_FOUND","Resource not found",r,List.of()); }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class) ResponseEntity<ApiErrorResponse> conflict(ObjectOptimisticLockingFailureException e,HttpServletRequest r) { return response(HttpStatus.CONFLICT,"OPTIMISTIC_LOCK_CONFLICT","The resource was changed by another request",r,List.of()); }
    @ExceptionHandler(PublishValidationException.class) ResponseEntity<ApiErrorResponse> publishValidation(PublishValidationException e,HttpServletRequest r) { return response(HttpStatus.UNPROCESSABLE_ENTITY,"PUBLISH_VALIDATION_FAILED","The post does not meet publishing requirements",r,List.of()); }
    @ExceptionHandler(IllegalStateException.class) ResponseEntity<ApiErrorResponse> state(IllegalStateException e,HttpServletRequest r) { return response(HttpStatus.CONFLICT,"STATE_CONFLICT","The requested state transition is not allowed",r,List.of()); }
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ApiErrorResponse> argument(IllegalArgumentException e,HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",r,List.of()); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) ResponseEntity<ApiErrorResponse> typeMismatch(MethodArgumentTypeMismatchException e,HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",r,List.of()); }
    private ResponseEntity<ApiErrorResponse> response(HttpStatus status,String code,String message,HttpServletRequest r,List<FieldValidationError> fields) { return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(),status.value(),code,message,r.getRequestURI(),fields)); }
}
