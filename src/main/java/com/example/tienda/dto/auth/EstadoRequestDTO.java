package com.example.tienda.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstadoRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}