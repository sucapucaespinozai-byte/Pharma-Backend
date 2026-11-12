package com.example.tienda.util;
import java.util.Set;
import org.springframework.data.domain.*;
import com.example.tienda.exception.ReglaNegocioException;
public final class PaginacionUtil {
 private PaginacionUtil() {}
 public static Pageable construir(int pagina,int tamanio,String campo,String direccion,java.util.Collection<String> permitidos) {
  if(pagina<0 || tamanio<1 || tamanio>100 || !permitidos.contains(campo) || !("asc".equalsIgnoreCase(direccion)||"desc".equalsIgnoreCase(direccion)))
   throw new ReglaNegocioException("Paginación u orden no válidos. Tamaño permitido: 1 a 100.");
  Sort sort=Sort.by(Sort.Direction.fromString(direccion),campo);
  if(!campo.equals("id")) sort=sort.and(Sort.by("id"));
  return PageRequest.of(pagina,tamanio,sort);
 }
}
