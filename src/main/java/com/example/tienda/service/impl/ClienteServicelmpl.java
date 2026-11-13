package com.example.tienda.service.impl;

import com.example.tienda.dto.ClienteRequestDTO;
import com.example.tienda.dto.ClienteResponseDTO;
import com.example.tienda.entity.Cliente;
import com.example.tienda.exception.RecursosNoEncontradoException;
import com.example.tienda.exception.ReglaNegocioException;
import com.example.tienda.repository.ClienteRepository;
import com.example.tienda.service.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@org.springframework.transaction.annotation.Transactional
public class ClienteServicelmpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final org.springframework.context.ApplicationEventPublisher eventos;

    public ClienteServicelmpl(ClienteRepository clienteRepository, org.springframework.context.ApplicationEventPublisher eventos) {
        this.clienteRepository = clienteRepository;
        this.eventos = eventos;
    }

    private ClienteResponseDTO convertirADto(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setCreadoPor(cliente.getCreadoPor());
        dto.setModificadoPor(cliente.getModificadoPor());
        dto.setDni(cliente.getDni());
        dto.setNombres(cliente.getNombres());
        dto.setApellidos(cliente.getApellidos());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setEstado(cliente.getEstado());
        return dto;
    }

    @Override
    public Page<ClienteResponseDTO> listar(int pagina, int tamanio, String ordenarPor, String direccion) {
        return listar(pagina,tamanio,ordenarPor,direccion,null);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public Page<ClienteResponseDTO> listar(int pagina,int tamanio,String ordenarPor,String direccion,String buscar) {
        var pageable=com.example.tienda.util.PaginacionUtil.construir(pagina,tamanio,ordenarPor,direccion,java.util.Set.of("id","nombres","apellidos","dni"));
        return clienteRepository.findAll((r,q,cb)->{
            if(buscar==null||buscar.isBlank())return cb.conjunction();
            String term="%"+buscar.trim().toLowerCase(java.util.Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
            return cb.or(cb.like(cb.lower(r.get("nombres")),term,'!'),cb.like(cb.lower(r.get("apellidos")),term,'!'),cb.like(r.get("dni"),term,'!'));
        },pageable).map(this::convertirADto);
    }

    @Override
    public ClienteResponseDTO buscar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con ID: " + id));
        return convertirADto(cliente);
    }

    @Override
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        if (clienteRepository.existsByDni(dto.getDni())) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el DNI: " + dto.getDni());
        }
        if (clienteRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el correo: " + dto.getEmail());
        }

        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        cliente.setEstado(dto.getEstado());

        Cliente guardado = clienteRepository.save(cliente);
        eventos.publishEvent(com.example.tienda.auditoria.EventoAuditoria.de(
                "CLIENTE_REGISTRADO", "Cliente", guardado.getId(), "Cliente registrado"));
        return convertirADto(guardado);
    }

    @Override
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con ID: " + id));

        if (clienteRepository.existsByDniAndIdNot(dto.getDni(), id)) {
            throw new ReglaNegocioException("Ya existe otro cliente registrado con el DNI: " + dto.getDni());
        }
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new ReglaNegocioException("Ya existe otro cliente registrado con el correo: " + dto.getEmail());
        }

        existente.setDni(dto.getDni());
        existente.setNombres(dto.getNombres());
        existente.setApellidos(dto.getApellidos());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        existente.setEstado(dto.getEstado());

        Cliente actualizado = clienteRepository.saveAndFlush(existente);
        return convertirADto(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(existente);
    }
}
