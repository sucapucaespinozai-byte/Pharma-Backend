package com.example.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tienda.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDni(String dni);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}