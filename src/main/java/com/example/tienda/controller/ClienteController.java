package com.example.tienda.controller;

import com.example.tienda.dto.ClienteRequestDTO;
import com.example.tienda.dto.ClienteResponseDTO;
import com.example.tienda.service.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(defaultValue = "apellidos") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion) {
        return ResponseEntity.ok(clienteService.listar(pagina, tamanio, ordenarPor, direccion));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return new ResponseEntity<>(clienteService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}