package com.example.tienda;
import com.example.tienda.auditoria.*;
import com.example.tienda.service.service.AuditoriaService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@SpringJUnitConfig(AuditoriaTransaccionTest.Config.class)
class AuditoriaTransaccionTest {
 @Configuration @org.springframework.transaction.annotation.EnableTransactionManagement @Import(AuditoriaListener.class) static class Config {
  @Bean AuditoriaService auditoriaService(){return mock(AuditoriaService.class);}
  @Bean PlatformTransactionManager tx(){return new AbstractPlatformTransactionManager(){
   protected Object doGetTransaction(){return new Object();}
   protected void doBegin(Object tx,TransactionDefinition d){}
   protected void doCommit(DefaultTransactionStatus s){}
   protected void doRollback(DefaultTransactionStatus s){}
  };}
 }
 @Autowired AuditoriaService auditoria;
 @Autowired PlatformTransactionManager manager;
 @Autowired ApplicationEventPublisher eventos;
 private final EventoAuditoria evento=new EventoAuditoria("vendedor","VENTA_REGISTRADA","Venta",1L,"total=5",null);
 @BeforeEach void resetear(){reset(auditoria);}
 @Test void registraSoloDespuesDelCommit(){new TransactionTemplate(manager).execute(s->{eventos.publishEvent(evento);verifyNoInteractions(auditoria);return null;});verify(auditoria).registrar(evento);}
 @Test void rollbackNoDejaVentaFalsa(){assertThatThrownBy(()->new TransactionTemplate(manager).execute(s->{eventos.publishEvent(evento);throw new IllegalStateException("rollback");})).isInstanceOf(IllegalStateException.class);verifyNoInteractions(auditoria);}
}
