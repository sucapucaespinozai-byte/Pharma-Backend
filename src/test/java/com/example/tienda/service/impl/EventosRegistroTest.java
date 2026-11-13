package com.example.tienda.service.impl;

import com.example.tienda.auditoria.EventoAuditoria;
import com.example.tienda.dto.*;
import com.example.tienda.entity.*;
import com.example.tienda.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class EventosRegistroTest {
 @Test void clientePublicaEventoSinDatosPersonales() {
  var repo=mock(ClienteRepository.class); var eventos=mock(ApplicationEventPublisher.class);
  when(repo.save(any())).thenAnswer(i->{Cliente c=i.getArgument(0);c.setId(17L);return c;});
  var dto=new ClienteRequestDTO();dto.setDni("12345678");dto.setEmail("privado@example.invalid");dto.setNombres("Nombre privado");
  new ClienteServicelmpl(repo,eventos).crear(dto);
  var captor=ArgumentCaptor.forClass(EventoAuditoria.class);verify(eventos).publishEvent(captor.capture());
  assertThat(captor.getValue().accion()).isEqualTo("CLIENTE_REGISTRADO");
  assertThat(captor.getValue().entidadId()).isEqualTo(17L);
  assertThat(captor.getValue().detalle()).doesNotContain(dto.getDni(),dto.getEmail(),dto.getNombres());
 }
 @Test void categoriaPublicaEventoConIdentificador() {
  var repo=mock(CategoriaRepository.class);var eventos=mock(ApplicationEventPublisher.class);
  when(repo.save(any())).thenAnswer(i->{Categoria c=i.getArgument(0);c.setId(19L);return c;});
  var dto=new CategoriaRequestDTO();dto.setNombre("Categoria demo");dto.setActivo(true);
  new CategoriaServicelmpl(repo,eventos).crear(dto);
  var captor=ArgumentCaptor.forClass(EventoAuditoria.class);verify(eventos).publishEvent(captor.capture());
  assertThat(captor.getValue().accion()).isEqualTo("CATEGORIA_REGISTRADA");
  assertThat(captor.getValue().entidadId()).isEqualTo(19L);
 }
 @Test void clienteDuplicadoNoPublicaEvento() {
  var repo=mock(ClienteRepository.class);var eventos=mock(ApplicationEventPublisher.class);
  var dto=new ClienteRequestDTO();dto.setDni("12345678");when(repo.existsByDni(dto.getDni())).thenReturn(true);
  assertThatThrownBy(()->new ClienteServicelmpl(repo,eventos).crear(dto)).isInstanceOf(RuntimeException.class);
  verifyNoInteractions(eventos);verify(repo,never()).save(any());
 }
}
