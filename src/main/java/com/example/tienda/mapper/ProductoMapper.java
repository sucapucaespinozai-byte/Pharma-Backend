package com.example.tienda.mapper;

import com.example.tienda.dto.CategoriaResumenDTO;
import com.example.tienda.dto.ProductoRequestDTO;
import com.example.tienda.dto.ProductoResponseDTO;
import com.example.tienda.entity.Categoria;
import com.example.tienda.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toResponseDTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());

        if (producto.getCategoria() != null) {
            CategoriaResumenDTO catDto = new CategoriaResumenDTO();
            catDto.setId(producto.getCategoria().getId());
            catDto.setNombre(producto.getCategoria().getNombre());
            dto.setCategoria(catDto);
        }
        return dto;
    }
}