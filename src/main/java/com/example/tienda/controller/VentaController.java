package com.example.tienda.controller;


import com.example.tienda.dto.VentaRequestDTO;
import com.example.tienda.dto.VentaResponseDTO;
import com.example.tienda.enums.EstadoVenta;
import com.example.tienda.service.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<VentaResponseDTO>> buscar(
            @RequestParam(required = false)
            Long clienteId,

            @RequestParam(required = false)
            EstadoVenta estado,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,

            @RequestParam(required = false, defaultValue = "fecha")
            String ordenarPor,

            @RequestParam(required = false, defaultValue = "desc")
            String direccion) {

        return ResponseEntity.ok(
                ventaService.buscar(
                        clienteId,
                        estado,
                        desde,
                        hasta,
                        ordenarPor,
                        direccion
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO request) {
        VentaResponseDTO response = ventaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody VentaRequestDTO request) {
        return ResponseEntity.ok(ventaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}