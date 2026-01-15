package com.robotech.robotech_backend.service.validadores;

import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailSuggestionService {

    private final UsuarioRepository usuarioRepo;
    private final ClubRepository clubRepo;
    private final EmailValidator emailValidator;

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]");
    private static final int MAX_LOCAL_PART = 64;

    public List<String> sugerirCorreosHumanosDisponibles(
            String correoIntentado,
            String nombres,
            String apellidos,
            int cantidad
    ) {
        emailValidator.validar(correoIntentado);

        String correoLower = correoIntentado.trim().toLowerCase(Locale.ROOT);
        String domain = correoLower.substring(correoLower.indexOf('@') + 1);

        // 🔥 CLAVE: revisar en AMBOS lados
        boolean existe =
                usuarioRepo.existsByCorreoIgnoreCase(correoLower)
                        || clubRepo.existsByCorreoContacto(correoLower);

        // si NO existe en ningún lado → no hay conflicto → no sugerimos
        if (!existe) {
            return List.of();
        }

        String n = normalizarToken(nombres);
        String a = normalizarToken(apellidos);

        List<String> nombresParts = splitParts(n);
        List<String> apellidosParts = splitParts(a);

        String primerNombre = nombresParts.isEmpty() ? "contacto" : nombresParts.get(0);
        String primerApellido = apellidosParts.isEmpty() ? "club" : apellidosParts.get(0);

        LinkedHashSet<String> bases = new LinkedHashSet<>();
        bases.add(primerNombre + "." + primerApellido);
        bases.add(primerNombre + primerApellido);
        bases.add(primerNombre + "_club");

        List<String> sugerencias = new ArrayList<>(cantidad);

        for (String base : bases) {
            if (sugerencias.size() >= cantidad) break;

            String baseOk = limitarLocalPart(base);

            for (int i = 1; i <= 50 && sugerencias.size() < cantidad; i++) {
                String cand = limitarLocalPart(baseOk + i) + "@" + domain;

                if (!usuarioRepo.existsByCorreoIgnoreCase(cand)
                        && !clubRepo.existsByCorreoContacto(cand)) {
                    sugerencias.add(cand);
                }
            }
        }

        return sugerencias;
    }

    private String normalizarToken(String texto) {
        if (texto == null) return "";
        String t = texto.trim().toLowerCase(Locale.ROOT);
        t = Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        t = t.replaceAll("[^a-z0-9\\s]", " ");
        return t.replaceAll("\\s+", " ").trim();
    }

    private List<String> splitParts(String t) {
        if (t.isBlank()) return List.of();
        return Arrays.stream(t.split(" "))
                .map(s -> NON_ALNUM.matcher(s).replaceAll(""))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String limitarLocalPart(String local) {
        return local.length() <= MAX_LOCAL_PART
                ? local
                : local.substring(0, MAX_LOCAL_PART);
    }
}
