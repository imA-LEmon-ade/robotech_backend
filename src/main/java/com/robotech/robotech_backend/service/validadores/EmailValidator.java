package com.robotech.robotech_backend.service.validadores;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public void validar(String correo) {

        if (correo == null || correo.isBlank()) {
            throw new FieldValidationException(
                    "correo",
                    "El correo es obligatorio",
                    List.of()
            );
        }

        if (!correo.matches(EMAIL_REGEX)) {
            throw new FieldValidationException(
                    "correo",
                    "Formato de correo inválido",
                    List.of("ejemplo@correo.com")
            );
        }
    }
}

