package com.example.tienda.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);
    private final SecurityErrorWriter errorWriter;

    public RestAccessDeniedHandler(SecurityErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        String username = request.getUserPrincipal() == null
                ? "anónimo"
                : request.getUserPrincipal().getName();
        log.warn("Acceso denegado: {} {} usuario={}", request.getMethod(), request.getRequestURI(), username);
        errorWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operación"
        );
    }
}
