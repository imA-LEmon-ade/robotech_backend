package com.robotech.robotech_backend.service.validadores;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class NicknameValidator {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;

    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "mierda", "puta", "puto", "perra", "maricon",
            "pendejo", "coño", "verga", "ctm", "conchatumadre",
            "culero", "chingar", "huevon", "idiota",
            "imbecil", "estupido", "hdp", "pinga"
    );

    private String normalizar(String texto) {
        if (texto == null) return "";

        String limpio = texto.toLowerCase();

        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        limpio = limpio.replaceAll("[^a-z0-9]", "");

        return limpio;
    }

    public void validar(String nickname) {

        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("El nickname es obligatorio");
        }

        if (nickname.length() < MIN_LENGTH) {
            throw new RuntimeException(
                    "El nickname debe tener al menos " + MIN_LENGTH + " caracteres"
            );
        }

        if (nickname.length() > MAX_LENGTH) {
            throw new RuntimeException(
                    "El nickname no puede superar los " + MAX_LENGTH + " caracteres"
            );
        }

        String limpio = normalizar(nickname);

        for (String palabra : PALABRAS_PROHIBIDAS) {
            String prohibida = normalizar(palabra);

            if (limpio.contains(prohibida)) {
                throw new RuntimeException(
                        "El nickname contiene palabras inapropiadas"
                );
            }
        }
    }
}
