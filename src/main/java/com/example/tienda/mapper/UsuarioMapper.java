package com.example.tienda.mapper;

import com.example.tienda.dto.auth.EmpleadoResponseDTO;
import com.example.tienda.dto.auth.RolResponseDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;
import com.example.tienda.entity.Empleado;
import com.example.tienda.entity.Rol;
import com.example.tienda.entity.Usuario;
import com.example.tienda.security.PermisosPorRol;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class UsuarioMapper {
    public UsuarioResponseDTO toUsuarioResponse(Usuario usuario) {
        Empleado empleado = usuario.getEmpleado();
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                empleado.getId(),
                empleado.getNombreCompleto(),
                empleado.getCargo(),
                nombresRoles(usuario.getRoles()),
                PermisosPorRol.permisos(usuario.getRoles()),
                usuario.getEstado(),
                usuario.getFechaCreacion()
        );
    }

    public EmpleadoResponseDTO toEmpleadoResponse(Empleado empleado, boolean tieneUsuario) {
        return new EmpleadoResponseDTO(
                empleado.getId(),
                empleado.getDni(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getCargo(),
                empleado.getEmail(),
                empleado.getTelefono(),
                empleado.getEstado(),
                tieneUsuario
        );
    }

    public RolResponseDTO toRolResponse(Rol rol) {
        return new RolResponseDTO(
                rol.getId(),
                rol.getNombre(),
                rol.getDescripcion(),
                PermisosPorRol.permisos(List.of(rol))
        );
    }

    public List<String> nombresRoles(Collection<Rol> roles) {
        return roles.stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();
    }
}
