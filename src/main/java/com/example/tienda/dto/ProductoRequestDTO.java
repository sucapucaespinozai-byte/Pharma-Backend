package com.example.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    private Integer stock;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;
}