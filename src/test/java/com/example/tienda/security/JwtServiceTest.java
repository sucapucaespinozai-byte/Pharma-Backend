package com.example.tienda.security;

import com.example.tienda.entity.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String OTHER_SECRET = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private final JwtService jwtService = new JwtService(SECRET, 60);

    private UserDetails vendedor() {
        return User.withUsername("vendedor")
                .password("not-used")
                .authorities(PermisosPorRol.authorities(List.of(new Rol(2L, "VENDEDOR", null))))
                .build();
    }

    @Test
    @DisplayName("El token contiene el usuario, las authorities y las fechas de expiración")
    void generarYLeerToken() {
        String token = jwtService.generateToken(vendedor());
        Claims claims = jwtService.readClaims(token);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.getSubject()).isEqualTo("vendedor");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        assertThat(claims.get("authorities", List.class))
                .contains("ROLE_VENDEDOR", "VENTAS_REGISTRAR")
                .doesNotContain("REPORTES_VER");
    }

    @Test
    @DisplayName("Un token alterado no pasa la verificación de firma")
    void tokenAlterado() {
        String[] sections = jwtService.generateToken(vendedor()).split("\\.");
        sections[1] = (sections[1].startsWith("A") ? "B" : "A") + sections[1].substring(1);

        assertThatThrownBy(() -> jwtService.extractUsername(String.join(".", sections)))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Se rechaza un token firmado con otra clave")
    void tokenFirmadoConOtraClave() {
        String token = new JwtService(OTHER_SECRET, 60).generateToken(vendedor());

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Un token vencido lanza ExpiredJwtException")
    void tokenExpirado() {
        String expired = new JwtService(SECRET, -1).generateToken(vendedor());

        assertThatThrownBy(() -> jwtService.extractUsername(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("La expiración configurada se informa en segundos")
    void expiracionEnSegundos() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }
}
