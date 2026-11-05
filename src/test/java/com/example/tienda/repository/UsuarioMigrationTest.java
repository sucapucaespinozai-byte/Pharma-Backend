package com.example.tienda.repository;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioMigrationTest {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void usuariosDePruebaPuedenAutenticarseConLasClavesDeLaGuia() throws IOException {
        String correction = new ClassPathResource("db/migration/V3__corregir_hashes_usuarios_prueba.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        Map<String, String> credentials = Map.of(
                "admin", "Admin2026*",
                "vendedor", "Vendedor2026*",
                "almacen", "Almacen2026*",
                "supervisor", "Supervisor2026*",
                "inactivo", "Inactivo2026*"
        );

        credentials.forEach((username, password) -> {
            Pattern pattern = Pattern.compile(
                    "set\\s+password\\s*=\\s*'([^']+)'\\s+where\\s+username\\s*=\\s*'"
                            + Pattern.quote(username) + "'",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = pattern.matcher(correction);
            assertThat(matcher.find()).as("hash corregido de %s en V3", username).isTrue();
            assertThat(passwordEncoder.matches(password, matcher.group(1)))
                    .as("contraseña de prueba de %s", username)
                    .isTrue();
        });
    }
}
