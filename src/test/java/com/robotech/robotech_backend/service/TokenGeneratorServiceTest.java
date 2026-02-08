package com.robotech.robotech_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenGeneratorServiceTest {

    @Test
    void generateSecureToken_devuelve_token() {
        TokenGeneratorService service = new TokenGeneratorService();
        String token = service.generateSecureToken();

        assertNotNull(token);
        assertTrue(token.length() >= 32);
    }
}
