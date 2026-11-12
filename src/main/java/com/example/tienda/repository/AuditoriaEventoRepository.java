package com.example.tienda.repository;
import com.example.tienda.entity.AuditoriaEvento;
import org.springframework.data.jpa.repository.*;
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento,Long>,JpaSpecificationExecutor<AuditoriaEvento> {}
