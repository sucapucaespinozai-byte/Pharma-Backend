package com.example.tienda.service.impl;

import com.example.tienda.dto.CategoriaRequestDTO;
import com.example.tienda.dto.CategoriaResponseDTO;
import com.example.tienda.entity.Categoria;
import com.example.tienda.exception.RecursosNoEncontradoException;
import com.example.tienda.repository.CategoriaRepository;
import com.example.tienda.service.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServicelmpl implements CategoriaService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CategoriaServicelmpl.class);
    private final CategoriaRepository categoriaRepository;

    public CategoriaServicelmpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    private CategoriaResponseDTO convertirADto(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setCreadoPor(categoria.getCreadoPor());
        dto.setModificadoPor(categoria.getModificadoPor());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setActivo(categoria.getActivo());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categorias", key = "'todas'")
    public List<CategoriaResponseDTO> listar() {
        long inicio = System.nanoTime();
        log.info("Inicio listar categorias");
        List<CategoriaResponseDTO> resultado = categoriaRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        log.info("Fin listar categorias | filas={} | duracionMs={}", resultado.size(),
                java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio));
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categorias", key = "'id-' + #p0")
    public CategoriaResponseDTO buscar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Categoría no encontrada con ID: " + id));
        return convertirADto(categoria);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categorias", allEntries = true)
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(dto.getActivo() != null && dto.getActivo() ? 1 : 0);
        Categoria guardada = categoriaRepository.save(categoria);
        return convertirADto(guardada);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"categorias", "productos"}, allEntries = true)
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Categoría no encontrada con ID: " + id));

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setActivo(dto.getActivo() != null && dto.getActivo() ? 1 : 0);

        Categoria actualizada = categoriaRepository.save(existente);
        categoriaRepository.flush();
        return convertirADto(actualizada);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"categorias", "productos"}, allEntries = true)
    public void eliminar(Long id) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Categoría no encontrada con ID: " + id));
        categoriaRepository.delete(existente);
    }
}
