package com.example.tienda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoriaResponseDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer activo;
    private LocalDateTime createdAt;
    private String creadoPor;
    private String modificadoPor;
}
