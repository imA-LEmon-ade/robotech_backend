package com.robotech.robotech_backend.service.validadores;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class NicknameValidator {

    // Lista base (puedes moverla a application.properties en el futuro)
    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "mierda", "puta", "puto", "perra", "perro", "maricon", "marica",
            "pendejo", "pendeja", "cono", "verga", "ctm", "conchatumadre",
            "culero", "chingar", "chingado", "chingada", "huevon", "huevona",
            "idiota", "imbecil", "estupido", "estupida", "hdp", "pinga",
            "carajo", "cabron", "cabr0n", "maldito", "maldita", "gil",
            "tarado", "tarada", "bastardo", "bastarda", "zorra", "putita",
            "putazo", "pinche", "pedo", "mam0n", "mamona", "pajero", "pajera",
            "pelotudo", "pelotuda", "cojudo", "cojuda", "cojones", "cojer",
            "verg4", "mierd4"
    );

    // Mapa para traducir numeros/simbolos a letras (leetspeak)
    private static final Map<Character, Character> LEETSPEAK_MAP = new HashMap<>();

    static {
        LEETSPEAK_MAP.put('0', 'o');
        LEETSPEAK_MAP.put('1', 'i');
        LEETSPEAK_MAP.put('2', 'z');
        LEETSPEAK_MAP.put('3', 'e');
        LEETSPEAK_MAP.put('4', 'a');
        LEETSPEAK_MAP.put('5', 's');
        LEETSPEAK_MAP.put('6', 'g');
        LEETSPEAK_MAP.put('7', 't');
        LEETSPEAK_MAP.put('8', 'b');
        LEETSPEAK_MAP.put('9', 'g');
        LEETSPEAK_MAP.put('@', 'a');
        LEETSPEAK_MAP.put('$', 's');
        LEETSPEAK_MAP.put('!', 'i');
    }

    private Pattern regexProhibidas;

    // Se ejecuta al iniciar Spring: compila la regex una sola vez
    @PostConstruct
    public void init() {
        String patternString = PALABRAS_PROHIBIDAS.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        this.regexProhibidas = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private String normalizarAvanzado(String texto) {
        if (texto == null) return "";

        // 1. Convertir a minusculas
        char[] chars = texto.toLowerCase().toCharArray();

        // 2. Traducir leetspeak (ej: "p3rr4" -> "perra")
        StringBuilder traducido = new StringBuilder();
        for (char c : chars) {
            traducido.append(LEETSPEAK_MAP.getOrDefault(c, c));
        }

        // 3. Normalizar acentos (a -> a)
        String nfd = Normalizer.normalize(traducido.toString(), Normalizer.Form.NFD);
        String sinAcentos = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 4. Eliminar todo lo que NO sea letra (deja solo a-z)
        String soloLetras = sinAcentos.replaceAll("[^a-z]", "");

        // 5. Colapsar letras repetidas: "puuuuta" -> "puta"
        return soloLetras.replaceAll("(.)\\1+", "$1");
    }

    public void validar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto no puede estar vacio");
        }

        String textoLimpio = normalizarAvanzado(texto);

        if (regexProhibidas.matcher(textoLimpio).find()) {
            throw new IllegalArgumentException("El texto contiene palabras inapropiadas.");
        }
    }
}
