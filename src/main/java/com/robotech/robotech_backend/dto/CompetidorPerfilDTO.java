package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CompetidorPerfilDTO {

    private String idCompetidor;
    private String nombres;
    private String apellidos;
    private String dni;
    private String estadoValidacion;
    private String clubActual;
    private int totalRobots;
    private int totalTorneos;
    private int puntosRanking;
    private String fotoUrl;
}
