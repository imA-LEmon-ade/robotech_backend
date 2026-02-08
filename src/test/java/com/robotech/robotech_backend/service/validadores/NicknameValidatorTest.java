package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NicknameValidatorTest {

    @Test
    void validar_ok() {
        NicknameValidator v = new NicknameValidator();
        v.init();
        assertDoesNotThrow(() -> v.validar("RobotMaster"));
    }

    @Test
    void validar_palabra_prohibida_lanza_error() {
        NicknameValidator v = new NicknameValidator();
        v.init();
        assertThrows(IllegalArgumentException.class, () -> v.validar("mi3rd4"));
    }
}
