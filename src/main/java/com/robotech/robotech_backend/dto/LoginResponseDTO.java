package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.RolUsuario;

import java.util.Set;

public record LoginResponseDTO(
        String token,
        Set<RolUsuario> roles,
        Object entidad
) {}


