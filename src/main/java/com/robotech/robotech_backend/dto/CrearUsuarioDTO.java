package com.robotech.robotech_backend.dto;

public record CrearUsuarioDTO(
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        String contrasena
) {}
