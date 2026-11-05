package com.example.tienda.service.service;

import com.example.tienda.dto.reporte.ProductoMasVendidoDTO;
import com.example.tienda.dto.reporte.VentaPorCategoriaDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    List<VentaPorCategoriaDTO> ventasPorCategoria(
            LocalDate desde,
            LocalDate hasta);

    List<ProductoMasVendidoDTO> productosMasVendidos(
            LocalDate desde,
            LocalDate hasta);
}