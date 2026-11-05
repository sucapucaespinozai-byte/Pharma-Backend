package com.example.tienda.service.service;

import com.example.tienda.dto.ProductoRequestDTO;
import com.example.tienda.dto.ProductoResponseDTO;
import com.example.tienda.service.generic.CrudService;

public interface ProductoService extends CrudService<ProductoRequestDTO, ProductoResponseDTO, Long> {
    com.example.tienda.dto.PaginaResponseDTO<ProductoResponseDTO> listar(
            int pagina, int tamanio, String ordenarPor, String direccion);
}
