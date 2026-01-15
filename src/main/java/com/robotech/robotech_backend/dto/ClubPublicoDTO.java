package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClubPublicoDTO {
    private String idClub;
    private String nombre;
    private long cantidadCompetidores;
}