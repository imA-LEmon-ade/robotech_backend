package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class InscritoDTO {
    private String idCompetidor;
    private String nombres;
    private String robot;
    private String categoria;
    private String estado;
}
