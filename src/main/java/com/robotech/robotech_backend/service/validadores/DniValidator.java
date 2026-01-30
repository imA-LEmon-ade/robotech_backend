package com.robotech.robotech_backend.service.validadores;

import org.springframework.stereotype.Component;

@Component
public class DniValidator {

    private static final String REGEX_DNI = "^\\d{8}$";

    public static void validar(String dni) {
        if (dni == null || !dni.matches(REGEX_DNI)) {
            throw new RuntimeException("El DNI debe tener 8 dígitos");
        }
    }
}
