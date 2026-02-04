package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record EditarUsuarioDTO(

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inv?lido")
        String correo,

        @NotBlank(message = "El tel?fono es obligatorio")
        @Pattern(
                regexp = "^9\\d{8}$",
                message = "El tel?fono debe tener 9 d?gitos y empezar con 9"
        )
        String telefono,

        Set<RolUsuario> roles,
        EstadoUsuario estado
) {}
