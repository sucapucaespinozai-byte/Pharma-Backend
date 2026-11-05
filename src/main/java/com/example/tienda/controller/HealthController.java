package com.example.tienda.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    @GetMapping
    public ResponseEntity<Map<String, String>> verificarEstado() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", "La aplicación está funcionando correctamente");
        return ResponseEntity.ok(respuesta);
    }
}
