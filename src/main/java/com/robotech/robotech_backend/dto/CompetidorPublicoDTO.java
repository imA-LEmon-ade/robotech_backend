package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetidorPublicoDTO {
    // Datos públicos
    private String nombreCompleto; // Unimos nombre y apellido
    private String club;

    // Estadísticas para presumir en la tarjeta
    private int totalRobots;
    private int puntosRanking;
    private int ranking; // Posición en la tabla
}