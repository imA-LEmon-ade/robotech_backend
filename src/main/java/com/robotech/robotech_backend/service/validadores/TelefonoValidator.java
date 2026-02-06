package com.robotech.robotech_backend.service.validadores;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class TelefonoValidator {

    private static final String REGEX_PERU = "^9\\d{8}$";

    public void validar(String telefono) {

        if (telefono == null || telefono.isBlank()) {
            throw new FieldValidationException(
                    "telefono",
                    "El teléfono es obligatorio",
                    List.of()
            );
        }

        if (!telefono.matches(REGEX_PERU)) {
            throw new FieldValidationException(
                    "telefono",
                    "El número debe tener 9 dígitos y empezar con 9",
                    List.of("987654321")
            );
        }
    }
}

