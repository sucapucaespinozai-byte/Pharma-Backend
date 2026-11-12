package com.example.tienda;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import com.example.tienda.util.PaginacionUtil;
class PaginacionUtilTest {
 @Test void rechazaTamanoYOrdenNoPermitidos(){
  for(int n:new int[]{0,101,500})assertThatThrownBy(()->PaginacionUtil.construir(0,n,"nombre","asc",Set.of("nombre"))).isInstanceOf(com.example.tienda.exception.ReglaNegocioException.class);
  assertThatThrownBy(()->PaginacionUtil.construir(0,20,"password","asc",Set.of("nombre"))).isInstanceOf(com.example.tienda.exception.ReglaNegocioException.class);
 }
 @Test void ordenEstableIncluyeId(){var p=PaginacionUtil.construir(0,20,"nombre","asc",Set.of("nombre"));assertThat(p.getSort().getOrderFor("id")).isNotNull();assertThat(p.getPageSize()).isEqualTo(20);}
}
