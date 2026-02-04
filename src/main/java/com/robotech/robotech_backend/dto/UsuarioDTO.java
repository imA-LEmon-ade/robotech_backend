package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;

import java.util.Set;

public record UsuarioDTO(
        String idUsuario,
        String dni,
        String nombres,
        String apellidos,
        String correo,
        Set<RolUsuario> roles,
        EstadoUsuario estado,
        String telefono
) {}
