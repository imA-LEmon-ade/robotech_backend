package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ColiseoDTO;

import java.util.List;

public interface ColiseoService {

    ColiseoDTO crear(ColiseoDTO dto);
    List<ColiseoDTO> listar();
    ColiseoDTO editar(String id, ColiseoDTO dto);
    void eliminar(String id);
}
