package com.robotech.robotech_backend.dto;

public record CrearAdminDTO(
        String dni,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        String contrasena
) {}
