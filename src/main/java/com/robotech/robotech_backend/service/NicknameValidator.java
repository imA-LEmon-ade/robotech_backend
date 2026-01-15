package com.robotech.robotech_backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class NicknameValidator {

    // Lista base (Podrías moverla a application.properties en el futuro)
    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "mierda", "puta", "puto", "perra", "maricon",
            "pendejo", "coño", "verga", "ctm", "conchatumadre",
            "culero", "chingar", "huevon", "idiota",
            "imbecil", "estupido", "hdp", "pinga", "tonto"
    );

    // Mapa para traducir números/símbolos a letras (Leetspeak)
    private static final Map<Character, Character> LEETSPEAK_MAP = new HashMap<>();

    static {
        LEETSPEAK_MAP.put('0', 'o');
        LEETSPEAK_MAP.put('1', 'i');
        LEETSPEAK_MAP.put('3', 'e');
        LEETSPEAK_MAP.put('4', 'a');
        LEETSPEAK_MAP.put('5', 's');
        LEETSPEAK_MAP.put('7', 't');
        LEETSPEAK_MAP.put('@', 'a');
        LEETSPEAK_MAP.put('$', 's');
        LEETSPEAK_MAP.put('!', 'i');
    }

    private Pattern regexProhibidas;

    // Se ejecuta al iniciar Spring: Compila la regex una sola vez para máxima velocidad
    @PostConstruct
    public void init() {
        // Crea una regex tipo: (mierda|puta|puto|...)
        // Flags: Case Insensitive y Unicode Case
        String patternString = String.join("|", PALABRAS_PROHIBIDAS);
        this.regexProhibidas = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /**
     * Limpia el texto, traduce leetspeak y normaliza.
     */
    private String normalizarAvanzado(String texto) {
        if (texto == null) return "";

        // 1. Convertir a minúsculas
        char[] chars = texto.toLowerCase().toCharArray();

        // 2. Traducir Leetspeak (Ej: 'p3rr4' -> 'perra')
        StringBuilder traducido = new StringBuilder();
        for (char c : chars) {
            traducido.append(LEETSPEAK_MAP.getOrDefault(c, c));
        }

        // 3. Normalizar acentos (á -> a)
        String nfd = Normalizer.normalize(traducido.toString(), Normalizer.Form.NFD);
        String sinAcentos = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 4. Eliminar todo lo que NO sea letra (deja solo a-z)
        // Esto une palabras: "hola mundo" -> "holamundo"
        return sinAcentos.replaceAll("[^a-z]", "");
    }

    public void validar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto no puede estar vacío");
        }

        // Versión 1: Búsqueda estricta (detecta "p.u.t.a", "p3rr4")
        String textoLimpio = normalizarAvanzado(texto);

        // Versión 2: Búsqueda exacta (para evitar falsos positivos como "computadora")
        // Nota: Si quieres ser muy estricto y no te importa bloquear "computadora",
        // usa solo 'textoLimpio'. Si quieres evitar falsos positivos, necesitas lógica más compleja.
        // Por ahora, usaremos la lógica estricta que tenías, pero mejorada con Leetspeak.

        // Buscamos si el texto limpio contiene alguna palabra de la regex
        if (regexProhibidas.matcher(textoLimpio).find()) {
            throw new IllegalArgumentException("El texto contiene palabras inapropiadas.");
        }
    }
}