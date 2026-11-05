package com.example.tienda.dto.reporte;

import java.math.BigDecimal;

public record VentaPorCategoriaDTO(
        Long categoriaId,
        String categoriaNombre,
        Long cantidadVendida,
        BigDecimal montoTotal
) {
}
