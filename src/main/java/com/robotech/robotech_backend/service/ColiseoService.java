package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.ColiseoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ColiseoService {

    ColiseoDTO crear(ColiseoDTO dto);
    Page<ColiseoDTO> listar(Pageable pageable, String q);
    ColiseoDTO editar(String id, ColiseoDTO dto);
    void eliminar(String id);
}


