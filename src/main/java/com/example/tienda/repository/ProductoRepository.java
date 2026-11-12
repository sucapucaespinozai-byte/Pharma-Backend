package com.example.tienda.repository;

import com.example.tienda.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Producto> {
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths="categoria")
    org.springframework.data.domain.Page<Producto> findAll(
            org.springframework.data.jpa.domain.Specification<Producto> filtro,
            org.springframework.data.domain.Pageable pagina);
}
