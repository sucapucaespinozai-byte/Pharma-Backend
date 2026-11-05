package com.example.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoResponseDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private CategoriaResumenDTO categoria;
}
