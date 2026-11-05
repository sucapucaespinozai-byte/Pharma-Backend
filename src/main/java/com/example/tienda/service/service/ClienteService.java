package com.example.tienda.service.service;

import com.example.tienda.dto.ClienteRequestDTO;
import com.example.tienda.dto.ClienteResponseDTO;
import org.springframework.data.domain.Page;

public interface ClienteService {
    Page<ClienteResponseDTO> listar(int pagina, int tamanio, String ordenarPor, String direccion);
    ClienteResponseDTO buscar(Long id);
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto);
    void eliminar(Long id);
}