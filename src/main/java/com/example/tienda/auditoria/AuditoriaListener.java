package com.example.tienda.auditoria;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import lombok.RequiredArgsConstructor;
import com.example.tienda.service.service.AuditoriaService;
@Component @RequiredArgsConstructor
public class AuditoriaListener {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(AuditoriaListener.class);
 private final AuditoriaService auditoriaService;
 @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
 public void alConfirmar(EventoAuditoria e){
  try {
   auditoriaService.registrar(e);
   log.info("Auditoria confirmada accion={} entidad={} entidadId={} usuario={} requestId={}",e.accion(),e.entidad(),e.entidadId(),e.usuario(),e.requestId());
  } catch(RuntimeException ex) {
   log.error("No se pudo auditar accion={} entidadId={} requestId={}",e.accion(),e.entidadId(),e.requestId(),ex);
  }
 }
}
