package com.example.tienda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@jakarta.persistence.EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @org.springframework.data.annotation.CreatedBy
    @Column(name="creado_por", length=50, updatable=false)
    private String creadoPor;

    @org.springframework.data.annotation.LastModifiedBy
    @Column(name="modificado_por", length=50)
    private String modificadoPor;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Boolean estado = true; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}
