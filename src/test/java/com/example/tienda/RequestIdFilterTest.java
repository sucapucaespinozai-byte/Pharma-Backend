package com.example.tienda;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.example.tienda.config.RequestIdFilter;
import static org.assertj.core.api.Assertions.*;
class RequestIdFilterTest {
    @Test void idCoincideConMdcYSeLimpiaInclusoSiFalla() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/categorias");
        request.addHeader("X-Request-Id", "valor-no-confiable");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> new RequestIdFilter().doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("requestId")).isEqualTo(response.getHeader("X-Request-Id"));
            assertThat(MDC.get("requestId")).isNotEqualTo("valor-no-confiable");
            throw new ServletException("fallo simulado");
        })).isInstanceOf(ServletException.class);
        assertThat(MDC.get("requestId")).isNull();
        assertThat(response.getHeader("X-Request-Id")).matches("[a-f0-9-]{36}");
    }
}
