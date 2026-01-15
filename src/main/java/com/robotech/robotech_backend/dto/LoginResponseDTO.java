package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.RolUsuario;

public record LoginResponseDTO(
        String token,
        RolUsuario rol,
        Object entidad
) {}

