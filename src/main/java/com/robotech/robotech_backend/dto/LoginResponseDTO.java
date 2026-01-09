package com.robotech.robotech_backend.dto;

public record LoginResponseDTO(
        String token,
        String rol,
        Object entidad
) {}

