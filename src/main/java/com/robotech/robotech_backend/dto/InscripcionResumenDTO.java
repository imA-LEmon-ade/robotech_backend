package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InscripcionResumenDTO {

    private String idInscripcion;
    private String torneo;
    private String categoria;
    private String modalidad; // INDIVIDUAL / EQUIPO
    private List<String> robots;
    private String estado;
}
