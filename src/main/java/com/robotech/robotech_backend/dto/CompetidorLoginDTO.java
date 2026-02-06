package com.robotech.robotech_backend.dto;

public record CompetidorLoginDTO(
        String idCompetidor,
        String nombres,
        String apellidos,
        String correo,
        String idClub,
        String nombreClub
) {}


