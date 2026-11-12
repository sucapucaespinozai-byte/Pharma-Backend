package com.example.tienda.auditoria;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
public record EventoAuditoria(String usuario,String accion,String entidad,Long entidadId,String detalle,String requestId) {
 public static EventoAuditoria de(String accion,String entidad,Long id,String detalle) {
  var a=SecurityContextHolder.getContext().getAuthentication();
  String usuario=a!=null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)?a.getName():"sistema";
  return new EventoAuditoria(recortar(usuario,50),accion,entidad,id,recortar(detalle,500),MDC.get("requestId"));
 }
 public static EventoAuditoria login(String accion,String username) {
  return new EventoAuditoria(recortar(username,50),accion,"Usuario",null,null,MDC.get("requestId"));
 }
 private static String recortar(String s,int n){return s==null?null:s.substring(0,Math.min(n,s.length())).replaceAll("[\\r\\n]"," ");}
}
