package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetidorPerfilDTO {

    private String idCompetidor;

    private String nombres;
    private String apellidos;
    private String dni;

    // Desde Usuario
    private String correo;
    private String telefono;

    // Club
    private String clubNombre;

    // Estado
    private String estadoValidacion;

    // Stats
    private Integer totalRobots;
    private Integer totalTorneos;

    // Opcional
    private Integer puntosRanking;
    private String fotoUrl;
}



