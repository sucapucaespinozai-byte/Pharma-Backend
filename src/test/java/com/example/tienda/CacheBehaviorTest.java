package com.example.tienda;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.example.tienda.config.CacheConfig;
import com.example.tienda.dto.CategoriaRequestDTO;
import com.example.tienda.entity.Categoria;
import com.example.tienda.repository.CategoriaRepository;
import com.example.tienda.service.impl.CategoriaServicelmpl;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(CacheBehaviorTest.Config.class)
class CacheBehaviorTest {
    @Configuration @Import({CacheConfig.class, CategoriaServicelmpl.class})
    static class Config {
        @Bean CategoriaRepository categoriaRepository() { return mock(CategoriaRepository.class); }
        @Bean CacheManager cacheManager() {
            SimpleCacheManager manager = new SimpleCacheManager();
            manager.setCaches(List.of(new FallableCache("categorias"), new ConcurrentMapCache("productos")));
            return manager;
        }
    }
    static class FallableCache extends ConcurrentMapCache {
        boolean unavailable;
        FallableCache(String name) { super(name); }
        @Override public ValueWrapper get(Object key) {
            if (unavailable) throw new IllegalStateException("Redis unavailable");
            return super.get(key);
        }
        @Override public void put(Object key, Object value) {
            if (unavailable) throw new IllegalStateException("Redis unavailable");
            super.put(key, value);
        }
        @Override public void clear() {
            if (unavailable) throw new IllegalStateException("Redis unavailable");
            super.clear();
        }
    }
    @Autowired com.example.tienda.service.service.CategoriaService service;
    @Autowired CategoriaRepository repository;
    @Autowired CacheManager manager;
    @BeforeEach void resetState() {
        reset(repository);
        FallableCache cache = (FallableCache) manager.getCache("categorias");
        cache.unavailable = false; cache.clear(); manager.getCache("productos").clear();
    }
    @Test void segundaConsultaUsaCacheYCrearInvalida() {
        when(repository.findAll()).thenReturn(List.of());
        service.listar(); service.listar();
        verify(repository, times(1)).findAll();
        CategoriaRequestDTO dto = new CategoriaRequestDTO(); dto.setNombre("Sistemas");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.crear(dto);
        assertThat(manager.getCache("categorias").get("todas")).isNull();
        service.listar(); verify(repository, times(2)).findAll();
    }
    @Test void actualizarLimpiaCursos() {
        Categoria categoria = new Categoria(); categoria.setId(1L); categoria.setNombre("Anterior");
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        manager.getCache("productos").put("todos", "datos anteriores");
        CategoriaRequestDTO dto = new CategoriaRequestDTO(); dto.setNombre("Nuevo");
        service.actualizar(1L, dto);
        assertThat(manager.getCache("productos").get("todos")).isNull();
    }
    @Test void erroresRedisNoImpidenLeerNiEscribirEnBaseDeDatos() {
        ((FallableCache) manager.getCache("categorias")).unavailable = true;
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.listar()).isEmpty(); assertThat(service.listar()).isEmpty();
        verify(repository, times(2)).findAll();
        CategoriaRequestDTO dto = new CategoriaRequestDTO(); dto.setNombre("Nueva");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThatCode(() -> service.crear(dto)).doesNotThrowAnyException();
        verify(repository).save(any());
    }
}

