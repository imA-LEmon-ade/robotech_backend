package com.robotech.robotech_backend.service.validadores;

import org.springframework.stereotype.Component;

@Component
public class TelefonoValidator {

    private static final String REGEX_PERU = "^9\\d{8}$";

    public static void validar(String telefono) {
        if (telefono == null || !telefono.matches(REGEX_PERU)) {
            throw new RuntimeException(
                    "El número de celular debe tener 9 dígitos y empezar con 9"
            );
        }
    }
}

