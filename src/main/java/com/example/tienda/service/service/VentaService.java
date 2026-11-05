package com.example.tienda.service.service;

import com.example.tienda.dto.VentaRequestDTO;
import com.example.tienda.dto.VentaResponseDTO;
import com.example.tienda.enums.EstadoVenta;
import com.example.tienda.service.generic.CrudService;

import java.time.LocalDate;
import java.util.List;

public interface VentaService extends CrudService<VentaRequestDTO, VentaResponseDTO, Long> {
    VentaResponseDTO registrar(VentaRequestDTO request);
    VentaResponseDTO buscar(Long id);
    List<VentaResponseDTO> listar();
    List<VentaResponseDTO> buscar(
            Long clienteId,
            EstadoVenta estado,
            LocalDate desde,
            LocalDate hasta,
            String ordenarPor,
            String direccion
    );
}