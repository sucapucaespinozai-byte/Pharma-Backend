package com.example.tienda.service.service;

import com.example.tienda.dto.CategoriaRequestDTO;
import com.example.tienda.dto.CategoriaResponseDTO;
import com.example.tienda.service.generic.CrudService;

public interface CategoriaService extends CrudService<CategoriaRequestDTO, CategoriaResponseDTO, Long> {
}