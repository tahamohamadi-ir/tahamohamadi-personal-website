package ir.tahamohamadi.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void mapsMultipartSizeOverflowToTheSafeMediaUploadError() throws Exception {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(21L * 1024 * 1024);
        ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class);
        Method method = resolver.resolveMethod(exception);

        assertThat(method).isNotNull();
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiErrorResponse> response = (ResponseEntity<ApiErrorResponse>) method.invoke(
                new GlobalExceptionHandler(),
                exception,
                new MockHttpServletRequest("POST", "/api/v1/admin/media")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody()).extracting(ApiErrorResponse::code).isEqualTo("MEDIA_TOO_LARGE");
        assertThat(response.getBody()).extracting(ApiErrorResponse::message).isEqualTo("Uploaded file was rejected");
    }
}
