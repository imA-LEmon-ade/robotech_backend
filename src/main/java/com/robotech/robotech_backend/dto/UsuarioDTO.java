package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;

public record UsuarioDTO(
        String idUsuario,
        String nombres,
        String apellidos,
        String correo,
        RolUsuario rol,
        EstadoUsuario estado,
        String telefono
) {}

