package com.example.tienda.controller;

import com.example.tienda.dto.auth.CambiarPasswordRequestDTO;
import com.example.tienda.dto.auth.LoginRequestDTO;
import com.example.tienda.dto.auth.LoginResponseDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;
import com.example.tienda.service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping; // Cambiado a PutMapping
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.usuarioActual(authentication.getName()));
    }

    @PutMapping("/password") // Requerido por la guía como PUT /api/v1/auth/password
    public ResponseEntity<Void> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody CambiarPasswordRequestDTO request) {
        authService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
