package com.robotech.robotech_backend.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class NicknameValidator {

    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "mierda", "puta", "puto", "perra", "maricon",
            "pendejo", "coño", "verga", "ctm", "conchatumadre",
            "culero", "chingar", "huevon", "idiota",
            "imbecil", "estupido", "hdp", "pinga", "idiota"
    );

    // Normaliza texto: quita tildes, símbolos y espacios
    private String normalizar(String texto) {
        if (texto == null) return "";

        // 1️⃣ Pasar a minúsculas
        String limpio = texto.toLowerCase();

        // 2️⃣ Quitar tildes (forma correcta)
        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 3️⃣ Quitar todo lo que no sea letras o números
        limpio = limpio.replaceAll("[^a-z0-9]", "");

        return limpio;
    }

    public void validar(String nickname) {

        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("El nickname es obligatorio");
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
