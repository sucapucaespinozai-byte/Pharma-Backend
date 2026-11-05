package com.example.tienda.service.impl;

import com.example.tienda.dto.auth.CambiarPasswordRequestDTO;
import com.example.tienda.dto.auth.LoginRequestDTO;
import com.example.tienda.dto.auth.LoginResponseDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;
import com.example.tienda.entity.Usuario;
import com.example.tienda.exception.RecursosNoEncontradoException;
import com.example.tienda.exception.ReglaNegocioException;
import com.example.tienda.mapper.UsuarioMapper;
import com.example.tienda.repository.UsuarioRepository;
import com.example.tienda.security.JwtService;
import com.example.tienda.security.PermisosPorRol;
import com.example.tienda.service.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String username = request.getUsername().trim();
        log.info("Intento de inicio de sesión: usuario={}", username);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        Usuario usuario = buscar(principal.getUsername());
        String token = jwtService.generateToken(principal);
        List<String> roles = usuarioMapper.nombresRoles(usuario.getRoles());

        log.info("Inicio de sesión correcto: usuario={} roles={}", usuario.getUsername(), roles);
        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                usuario.getUsername(),
                usuario.getEmpleado().getNombreCompleto(),
                roles,
                PermisosPorRol.permisos(usuario.getRoles())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO usuarioActual(String username) {
        return usuarioMapper.toUsuarioResponse(buscar(username));
    }

    @Override
    @Transactional
    public void cambiarPassword(String username, CambiarPasswordRequestDTO request) {
        log.info("Solicitud de cambio de contraseña para el usuario: {}", username);
        Usuario usuario = buscar(username);

        // 1. Validar que la contraseña actual coincida con el hash -> 409 según la guía
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta");
        }

        // 2. Validar que la nueva contraseña sea distinta de la actual -> 409
        if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPassword())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual");
        }

        // 3. Validar límite de bytes por seguridad (máximo 72 bytes para BCrypt)
        if (request.getPasswordNueva().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La nueva contraseña no puede superar 72 bytes");
        }

        // 4. Guardar el nuevo hash cifrado
        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada exitosamente para el usuario: {}", username);
    }

    private Usuario buscar(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursosNoEncontradoException(
                        "Usuario no encontrado: " + username));
    }
}