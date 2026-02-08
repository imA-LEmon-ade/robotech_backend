package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @Test
    void validar_ok() {
        assertDoesNotThrow(() -> validator.validar("test@example.com"));
    }

    @Test
    void validar_invalido_lanza_error() {
        assertThrows(FieldValidationException.class, () -> validator.validar("bad-email"));
    }
}
