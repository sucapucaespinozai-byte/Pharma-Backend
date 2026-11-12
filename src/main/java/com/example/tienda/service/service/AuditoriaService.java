package com.example.tienda.service.service;
import java.time.LocalDate;
import com.example.tienda.auditoria.EventoAuditoria;
import com.example.tienda.dto.*;
public interface AuditoriaService {
 void registrar(EventoAuditoria evento);
 PaginaResponseDTO<AuditoriaEventoDTO> buscar(String usuario,String accion,LocalDate desde,LocalDate hasta,int pagina,int tamanio);
}
