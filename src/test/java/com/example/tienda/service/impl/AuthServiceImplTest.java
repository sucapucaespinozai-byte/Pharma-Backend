package com.example.tienda.service.impl;

import com.example.tienda.dto.auth.CambiarPasswordRequestDTO;
import com.example.tienda.entity.Usuario;
import com.example.tienda.repository.UsuarioRepository;
import com.example.tienda.security.JwtService;
import com.example.tienda.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import com.example.tienda.exception.ReglaNegocioException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthServiceImpl authService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                jwtService,
                usuarioRepository,
                usuarioMapper,
                passwordEncoder, org.mockito.Mockito.mock(com.example.tienda.service.service.AuditoriaService.class)
        );
        usuario = new Usuario();
        usuario.setUsername("vendedor");
        usuario.setPassword(passwordEncoder.encode("Vendedor2026*"));
        when(usuarioRepository.findByUsernameIgnoreCase("vendedor")).thenReturn(Optional.of(usuario));
    }

    @Test
    void cambiarPasswordGuardaUnHashNuevo() {
        authService.cambiarPassword(
                "vendedor",
                new CambiarPasswordRequestDTO("Vendedor2026*", "Nueva2026*")
        );

        assertThat(passwordEncoder.matches("Nueva2026*", usuario.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("Vendedor2026*", usuario.getPassword())).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPasswordRechazaLaClaveActualIncorrecta() {
        assertThatThrownBy(() -> authService.cambiarPassword(
                "vendedor",
                new CambiarPasswordRequestDTO("incorrecta", "Nueva2026*")
        )).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cambiarPasswordRechazaReutilizarLaClaveActual() {
        assertThatThrownBy(() -> authService.cambiarPassword(
                "vendedor",
                new CambiarPasswordRequestDTO("Vendedor2026*", "Vendedor2026*")
        )).isInstanceOf(com.example.tienda.exception.ReglaNegocioException.class);
    }
}
