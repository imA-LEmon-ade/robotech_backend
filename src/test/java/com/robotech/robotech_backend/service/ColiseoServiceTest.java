package com.robotech.robotech_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ColiseoServiceTest {

    @Test
    void coliseoService_es_interfaz() {
        assertTrue(ColiseoService.class.isInterface());
    }
}
