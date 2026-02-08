package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelefonoValidatorTest {

    private final TelefonoValidator validator = new TelefonoValidator();

    @Test
    void validar_ok() {
        assertDoesNotThrow(() -> validator.validar("999111222"));
    }

    @Test
    void validar_invalido_lanza_error() {
        assertThrows(FieldValidationException.class, () -> validator.validar("123"));
    }

    @Test
    void validar_vacio_lanza_error() {
        assertThrows(FieldValidationException.class, () -> validator.validar(""));
    }
}
