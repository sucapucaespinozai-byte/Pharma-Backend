package com.example.tienda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String CABECERA = "X-Request-Id";
    public static final String CLAVE_MDC = "requestId";
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String anterior = MDC.get(CLAVE_MDC);
        String id = UUID.randomUUID().toString();
        MDC.put(CLAVE_MDC, id);
        response.setHeader(CABECERA, id);
        long inicio = System.nanoTime();
        boolean fallo = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            fallo = true;
            throw ex;
        } finally {
            try {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(),
                        fallo ? 500 : response.getStatus(),
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio));
            } finally {
                if (anterior == null) MDC.remove(CLAVE_MDC);
                else MDC.put(CLAVE_MDC, anterior);
            }
        }
    }
}
