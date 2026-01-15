package com.robotech.robotech_backend.service.validadores;

import org.springframework.stereotype.Component;

@Component
public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public static void validar(String correo) {
        if (correo == null || !correo.matches(EMAIL_REGEX)) {
            throw new RuntimeException("Correo electrónico inválido");
        }
    }
}
