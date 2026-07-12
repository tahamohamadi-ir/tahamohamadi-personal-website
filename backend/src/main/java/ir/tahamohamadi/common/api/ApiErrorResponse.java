package ir.tahamohamadi.common.api;
import java.time.Instant;
import java.util.List;
public record ApiErrorResponse(Instant timestamp, int status, String code, String message, String path, List<FieldValidationError> fields) { }
