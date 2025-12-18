package com.robotech.robotech_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class EquipoInscripcionDTO {

    private String idCategoriaTorneo;
    private List<String> robots; // ids
}

