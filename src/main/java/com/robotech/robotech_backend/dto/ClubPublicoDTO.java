package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubPublicoDTO {
    private String idClub;
    private String nombre;
    private String direccionFiscal;
    private String correoContacto;
    private long cantidadCompetidores;
}

