package com.example.tienda.service.service;

import com.example.tienda.dto.auth.CambiarPasswordRequestDTO;
import com.example.tienda.dto.auth.LoginRequestDTO;
import com.example.tienda.dto.auth.LoginResponseDTO;
import com.example.tienda.dto.auth.UsuarioResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);

    UsuarioResponseDTO usuarioActual(String username);

    void cambiarPassword(String username, CambiarPasswordRequestDTO request);
}
