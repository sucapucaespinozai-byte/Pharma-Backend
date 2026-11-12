package com.example.tienda.dto;

import java.io.Serializable;
import java.util.List;

public record PaginaResponseDTO<T>(List<T> content, int number,
        int size, long totalElements, int totalPages, boolean last) implements Serializable {
    private static final long serialVersionUID = 1L;
    public static <T> PaginaResponseDTO<T> de(org.springframework.data.domain.Page<T> p) {
        return new PaginaResponseDTO<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.isLast());
    }
}
