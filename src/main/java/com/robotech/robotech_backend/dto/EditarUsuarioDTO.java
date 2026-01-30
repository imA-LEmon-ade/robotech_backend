package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EditarUsuarioDTO(

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^9\\d{8}$",
                message = "El teléfono debe tener 9 dígitos y empezar con 9"
        )
        String telefono,

        RolUsuario rol,
        EstadoUsuario estado
) {}