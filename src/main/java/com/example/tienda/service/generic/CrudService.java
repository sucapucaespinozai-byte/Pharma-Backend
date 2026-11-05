package com.example.tienda.service.generic;

import java.util.List;

public interface CrudService<ReqDTO, ResDTO, ID> {
    List<ResDTO> listar();
    ResDTO buscar(ID id);
    ResDTO crear(ReqDTO dto);
    ResDTO actualizar(ID id, ReqDTO dto);
    void eliminar(ID id);
}