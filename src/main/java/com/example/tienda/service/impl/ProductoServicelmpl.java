package com.example.tienda.service.impl;

import com.example.tienda.dto.ProductoRequestDTO;
import com.example.tienda.dto.ProductoResponseDTO;
import com.example.tienda.entity.Categoria;
import com.example.tienda.entity.Producto;
import com.example.tienda.exception.RecursosNoEncontradoException;
import com.example.tienda.mapper.ProductoMapper;
import com.example.tienda.repository.CategoriaRepository;
import com.example.tienda.repository.ProductoRepository;
import com.example.tienda.service.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServicelmpl implements ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "#p0 + '-' + #p1 + '-' + #p2 + '-' + #p3")
    public com.example.tienda.dto.PaginaResponseDTO<ProductoResponseDTO> listar(
            int pagina, int tamanio, String ordenarPor, String direccion) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100
                || !List.of("id", "nombre", "precio", "stock").contains(ordenarPor)
                || !("asc".equalsIgnoreCase(direccion) || "desc".equalsIgnoreCase(direccion))) {
            throw new com.example.tienda.exception.ReglaNegocioException("Paginación u orden no válidos");
        }
        var sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.fromString(direccion), ordenarPor);
        var resultado = productoRepository.findAll(
                org.springframework.data.domain.PageRequest.of(pagina, tamanio, sort));
        return new com.example.tienda.dto.PaginaResponseDTO<>(
                resultado.getContent().stream().map(productoMapper::toResponseDTO).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), resultado.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'todas'")
    public List<ProductoResponseDTO> listar() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productos", key = "'id-' + #p0")
    public ProductoResponseDTO buscar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Producto no encontrado con ID: " + id));
        return productoMapper.toResponseDTO(producto);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public ProductoResponseDTO crear(ProductoRequestDTO requestDTO) {
        Categoria categoria = categoriaRepository.findById(requestDTO.getCategoriaId())
                .orElseThrow(() -> new RecursosNoEncontradoException("Categoría no encontrada con ID: " + requestDTO.getCategoriaId()));

        Producto producto = new Producto();
        producto.setNombre(requestDTO.getNombre());
        producto.setPrecio(requestDTO.getPrecio());
        producto.setStock(requestDTO.getStock());
        producto.setCategoria(categoria);

        Producto productoGuardado = productoRepository.save(producto);
        return productoMapper.toResponseDTO(productoGuardado);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO requestDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(requestDTO.getCategoriaId())
                .orElseThrow(() -> new RecursosNoEncontradoException("Categoría no encontrada con ID: " + requestDTO.getCategoriaId()));

        productoExistente.setNombre(requestDTO.getNombre());
        productoExistente.setPrecio(requestDTO.getPrecio());
        productoExistente.setStock(requestDTO.getStock());
        productoExistente.setCategoria(categoria);

        Producto productoActualizado = productoRepository.save(productoExistente);
        return productoMapper.toResponseDTO(productoActualizado);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Producto no encontrado con ID: " + id));
        productoRepository.delete(producto);
    }
}
