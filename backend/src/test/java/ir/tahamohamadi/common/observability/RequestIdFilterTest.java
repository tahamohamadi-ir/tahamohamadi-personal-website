package ir.tahamohamadi.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestIdFilterTest {

    @Test
    void shouldGenerateRequestIdIfMissing() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        when(request.getHeader("X-Request-ID")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(chain).doFilter(request, response);
        assertNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
    }

    @Test
    void shouldReuseProvidedRequestId() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        when(request.getHeader("X-Request-ID")).thenReturn("custom-id-123");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader("X-Request-ID", "custom-id-123");
        verify(chain).doFilter(request, response);
        assertNull(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY));
    }
}
