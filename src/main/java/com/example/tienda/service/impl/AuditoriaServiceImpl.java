package com.example.tienda.service.impl;
import java.time.*;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import lombok.RequiredArgsConstructor;
import com.example.tienda.auditoria.EventoAuditoria;
import com.example.tienda.dto.*;
import com.example.tienda.repository.AuditoriaEventoRepository;
import com.example.tienda.service.service.AuditoriaService;
import com.example.tienda.util.PaginacionUtil;
import com.example.tienda.exception.ReglaNegocioException;
@Service @RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {
 private final AuditoriaEventoRepository repository;
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public void registrar(EventoAuditoria e){
  var a=new com.example.tienda.entity.AuditoriaEvento();a.setFecha(LocalDateTime.now());a.setUsuario(e.usuario());a.setAccion(e.accion());a.setEntidad(e.entidad());a.setEntidadId(e.entidadId());a.setDetalle(e.detalle());a.setRequestId(e.requestId());repository.save(a);
 }
 @Transactional(readOnly=true)
 public PaginaResponseDTO<AuditoriaEventoDTO> buscar(String usuario,String accion,LocalDate desde,LocalDate hasta,int pagina,int tamanio){
  if(desde!=null&&hasta!=null&&desde.isAfter(hasta))throw new ReglaNegocioException("Rango de fechas inválido");
  var pageable=PaginacionUtil.construir(pagina,tamanio,"fecha","desc",Set.of("fecha","usuario","accion"));
  var resultado=repository.findAll((r,q,cb)->{
   List<Predicate> p=new ArrayList<>();
   if(usuario!=null&&!usuario.isBlank())p.add(cb.equal(cb.upper(r.get("usuario")),usuario.trim().toUpperCase(Locale.ROOT)));
   if(accion!=null&&!accion.isBlank())p.add(cb.equal(r.get("accion"),accion.trim()));
   if(desde!=null)p.add(cb.greaterThanOrEqualTo(r.get("fecha"),desde.atStartOfDay()));
   if(hasta!=null)p.add(cb.lessThan(r.get("fecha"),hasta.plusDays(1).atStartOfDay()));
   return cb.and(p.toArray(Predicate[]::new));
  },pageable).map(AuditoriaEventoDTO::de);
  return PaginaResponseDTO.de(resultado);
 }
}
