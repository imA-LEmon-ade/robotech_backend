package com.robotech.robotech_backend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NicknameValidator {

    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "mierda", "puta", "puto", "perra", "maricon", "maricón",
            "pendejo", "coño", "verga", "ctm", "conchatumadre",
            "culero", "chingar", "huevon", "huevón", "idiota",
            "imbecil", "estupido", "estúpido"
    );

    // Limpia símbolos comunes usados para burlar el filtro
    private String normalizar(String texto) {
        return texto
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .replaceAll("á","a")
                .replaceAll("é","e")
                .replaceAll("í","i")
                .replaceAll("ó","o")
                .replaceAll("ú","u");
    }

    public void validar(String nickname) {

        String limpio = normalizar(nickname);

        for (String palabra : PALABRAS_PROHIBIDAS) {
            String prohibida = normalizar(palabra);

            if (limpio.contains(prohibida)) {
                throw new RuntimeException("El nickname contiene palabras inapropiadas");
            }
        }
    }
}
