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
    private final org.springframework.context.ApplicationEventPublisher eventos;

    @Override
    @Transactional(readOnly=true)
    @Cacheable(cacheNames="productos", key="'s14-' + #p0 + '-' + #p1 + '-' + #p2 + '-' + #p3 + '-' + #p4 + '-' + #p5")
    public com.example.tienda.dto.PaginaResponseDTO<ProductoResponseDTO> listar(int pagina,int tamanio,String ordenarPor,String direccion,String buscar,boolean disponibles) {
        var pageable=com.example.tienda.util.PaginacionUtil.construir(pagina,tamanio,ordenarPor,direccion,java.util.Set.of("id","nombre","precio","stock"));
        var resultado=productoRepository.findAll((r,q,cb)->{
            var p=new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if(buscar!=null&&!buscar.isBlank()) {
                String term="%"+buscar.trim().toLowerCase(java.util.Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
                p.add(cb.or(cb.like(cb.lower(r.get("nombre")),term,'!'),cb.like(cb.lower(r.get("categoria").get("nombre")),term,'!')));
            }
            if(disponibles){p.add(cb.isTrue(r.get("estado")));p.add(cb.greaterThan(r.get("stock"),0));p.add(cb.equal(r.get("categoria").get("activo"),1));}
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        },pageable);
        return com.example.tienda.dto.PaginaResponseDTO.de(resultado.map(productoMapper::toResponseDTO));
    }
    @Override
    @Transactional(readOnly=true)
    public com.example.tienda.dto.PaginaResponseDTO<ProductoResponseDTO> listar(int pagina,int tamanio,String ordenarPor,String direccion){return listar(pagina,tamanio,ordenarPor,direccion,null,false);}

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

        var precioAnterior=productoExistente.getPrecio();
        productoExistente.setNombre(requestDTO.getNombre());
        productoExistente.setPrecio(requestDTO.getPrecio());
        productoExistente.setStock(requestDTO.getStock());
        productoExistente.setCategoria(categoria);

        Producto productoActualizado = productoRepository.saveAndFlush(productoExistente);
        if(precioAnterior.compareTo(productoActualizado.getPrecio())!=0) eventos.publishEvent(com.example.tienda.auditoria.EventoAuditoria.de("PRECIO_MODIFICADO","Producto",id,precioAnterior+" -> "+productoActualizado.getPrecio()));
        return productoMapper.toResponseDTO(productoActualizado);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradoException("Producto no encontrado con ID: " + id));
        producto.setEstado(false);
        productoRepository.save(producto);
        eventos.publishEvent(com.example.tienda.auditoria.EventoAuditoria.de("PRODUCTO_ELIMINADO","Producto",id,producto.getNombre()));
    }
}
