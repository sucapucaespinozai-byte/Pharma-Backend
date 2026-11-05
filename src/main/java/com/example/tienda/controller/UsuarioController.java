package com.example.tienda.controller;

import com.example.tienda.dto.auth.AsignarRolesRequestDTO;
import com.example.tienda.dto.auth.EmpleadoResponseDTO;
import com.example.tienda.dto.auth.EstadoRequestDTO;
import com.example.tienda.dto.auth.RolResponseDTO;
import com.example.tienda.dto.auth.UsuarioRequestDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;
import com.example.tienda.service.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @GetMapping("/empleados")
    public ResponseEntity<List<EmpleadoResponseDTO>> empleados() {
        return ResponseEntity.ok(usuarioService.listarEmpleados());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponseDTO>> roles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    @PutMapping("/usuarios/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> asignarRoles(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRolesRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.asignarRoles(id, request.getRolIds(), authentication.getName()));
    }

    @PatchMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('USUARIOS_ADMINISTRAR')")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request.getEstado(), authentication.getName()));
    }
}