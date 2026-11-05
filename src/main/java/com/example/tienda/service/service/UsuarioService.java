package com.example.tienda.service.service;

import com.example.tienda.dto.auth.EmpleadoResponseDTO;
import com.example.tienda.dto.auth.RolResponseDTO;
import com.example.tienda.dto.auth.UsuarioRequestDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;

import java.util.List;
import java.util.Set;

public interface UsuarioService {
    UsuarioResponseDTO crear(UsuarioRequestDTO request);

    List<UsuarioResponseDTO> listar();

    List<EmpleadoResponseDTO> listarEmpleados();

    List<RolResponseDTO> listarRoles();

    UsuarioResponseDTO asignarRoles(Long id, Set<Long> rolIds, String usernameAdminActual);

    UsuarioResponseDTO cambiarEstado(Long id, Boolean estado, String usernameAdminActual);
}