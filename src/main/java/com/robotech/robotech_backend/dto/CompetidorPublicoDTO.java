package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List; // Importante añadir esto

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetidorPublicoDTO {
    private String nombreCompleto;
    private String club;

    // ✅ NUEVO: Lista de nombres de robots del competidor
    private List<String> nombresRobots;

    private int totalRobots;
    private int puntosRanking;
    private int ranking;
}

