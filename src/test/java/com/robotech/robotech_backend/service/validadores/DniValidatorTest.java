package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DniValidatorTest {

    @Test
    void validar_ok() {
        assertDoesNotThrow(() -> DniValidator.validar("12345678"));
    }

    @Test
    void validar_invalido_lanza_error() {
        assertThrows(RuntimeException.class, () -> DniValidator.validar("123"));
    }
}
