package com.robotech.robotech_backend.dto;

public record CompetidorClubDTO(
        String idCompetidor,
        String nombres,
        String apellidos,
        String dni,
        String estadoValidacion,
        String correo
) {}