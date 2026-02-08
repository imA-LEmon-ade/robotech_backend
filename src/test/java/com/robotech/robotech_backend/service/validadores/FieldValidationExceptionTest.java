package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldValidationExceptionTest {

    @Test
    void getters_retorna_valores() {
        FieldValidationException ex = new FieldValidationException("correo", "msg", List.of("a@a.com"));
        assertEquals("correo", ex.getField());
        assertEquals(1, ex.getSuggestions().size());
    }
}
