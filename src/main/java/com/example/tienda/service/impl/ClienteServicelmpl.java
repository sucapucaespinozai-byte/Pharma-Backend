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
public class ClienteServicelmpl implements ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteServicelmpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    private ClienteResponseDTO convertirADto(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
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
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();

        Pageable pageable = PageRequest.of(pagina, tamanio, sort);

        return clienteRepository.findAll(pageable)
                .map(this::convertirADto);
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

        Cliente actualizado = clienteRepository.save(existente);
        return convertirADto(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(existente);
    }
}