package com.example.tienda.dto;

import java.io.Serializable;
import java.util.List;

public record PaginaResponseDTO<T extends Serializable>(List<T> content, int number,
        int size, long totalElements, int totalPages, boolean last) implements Serializable {
    private static final long serialVersionUID = 1L;
}
