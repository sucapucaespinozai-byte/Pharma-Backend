package com.example.tienda.controller;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import com.example.tienda.dto.*;
import com.example.tienda.service.service.AuditoriaService;
@RestController @RequestMapping("/api/v1/auditoria") @RequiredArgsConstructor
public class AuditoriaController {
 private final AuditoriaService service;
 @GetMapping public PaginaResponseDTO<AuditoriaEventoDTO> buscar(@RequestParam(required=false)String usuario,@RequestParam(required=false)String accion,
 @RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate desde,
 @RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate hasta,
 @RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="20")int tamanio){return service.buscar(usuario,accion,desde,hasta,pagina,tamanio);}
}
