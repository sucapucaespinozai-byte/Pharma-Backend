package com.example.tienda.controller;

import com.example.tienda.dto.ProductoRequestDTO;
import com.example.tienda.dto.ProductoResponseDTO;
import com.example.tienda.service.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<com.example.tienda.dto.PaginaResponseDTO<ProductoResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required=false) String buscar, @RequestParam(defaultValue="false") boolean disponibles) {
        return ResponseEntity.ok(productoService.listar(pagina, tamanio, ordenarPor, direccion, buscar, disponibles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscar(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.buscar(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO nuevo = productoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO actualizado = productoService.actualizar(id, requestDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
