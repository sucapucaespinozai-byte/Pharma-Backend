package com.example.tienda.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AsignarRolesRequestDTO {

    @NotEmpty(message = "La lista de roles no puede estar vacía")
    private Set<@NotNull Long> rolIds;
}