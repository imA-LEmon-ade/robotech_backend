package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TorneoPublicoDTO;
import com.robotech.robotech_backend.dto.ResultadoTorneoDTO;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.repository.TorneoRepository;
import com.robotech.robotech_backend.repository.HistorialCalificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TorneoPublicoService {

    private final TorneoRepository torneoRepo;
    private final HistorialCalificacionRepository historialRepo;

    @Transactional(readOnly = true)
    public List<TorneoPublicoDTO> obtenerTodos() {
        List<String> estadosVisibles = List.of(
                "INSCRIPCIONES_ABIERTAS",
                "INSCRIPCIONES_CERRADAS",
                "EN_PROGRESO",
                "FINALIZADO"
        );

        return torneoRepo.findByEstadoIn(estadosVisibles).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TorneoPublicoDTO obtenerPorId(String id) {
        Torneo t = torneoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        return mapToDTO(t);
    }

    private LocalDate convertirFecha(java.util.Date fecha) {
        if (fecha == null) return null;
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private TorneoPublicoDTO mapToDTO(Torneo t) {
        LocalDate fechaInicio = convertirFecha(t.getFechaInicio());
        LocalDate fechaFin = convertirFecha(t.getFechaFin());

        List<String> categorias = new ArrayList<>();
        if (t.getCategorias() != null) {
            categorias = t.getCategorias().stream()
                    .map(ct -> ct.getCategoria() != null ? ct.getCategoria().name() : "Sin nombre")
                    .collect(Collectors.toList());
        }

        String estadoStr = (t.getEstado() != null) ? t.getEstado().toString() : "DESCONOCIDO";
        String descripcion = (t.getDescripcion() != null) ? t.getDescripcion() : "Sin descripción";

        String ganador = null;
        List<ResultadoTorneoDTO> resultados = new ArrayList<>();

        // --- DEPURACIÓN DE RESULTADOS ---
        if ("FINALIZADO".equals(estadoStr)) {
            System.out.println("====== DEBUG ROBOTECH ======");
            System.out.println("Buscando resultados para Torneo: " + t.getNombre() + " (ID: " + t.getIdTorneo() + ")");

            resultados = historialRepo.obtenerRankingRobots(t.getIdTorneo());

            if (resultados != null) {
                System.out.println("Resultados encontrados en BD: " + resultados.size());
                resultados.forEach(r -> System.out.println(" - Robot: " + r.getNombre() + " | Puntos: " + r.getPuntaje()));

                if (!resultados.isEmpty()) {
                    ganador = resultados.get(0).getNombre();
                    System.out.println("Ganador detectado: " + ganador);
                }
            } else {
                System.out.println("ERROR: La lista de resultados es NULL");
            }
            System.out.println("============================");
        }

        return new TorneoPublicoDTO(
                t.getIdTorneo(),
                t.getNombre(),
                fechaInicio,
                fechaFin,
                estadoStr,
                descripcion,
                categorias,
                ganador,
                resultados
        );
    }
}