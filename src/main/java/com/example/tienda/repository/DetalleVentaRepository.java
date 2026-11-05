package com.example.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tienda.entity.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
}