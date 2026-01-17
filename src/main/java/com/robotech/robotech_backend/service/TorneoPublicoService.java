package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.TorneoPublicoDTO;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.repository.TorneoRepository;
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

    @Transactional(readOnly = true)
    public List<TorneoPublicoDTO> obtenerTodos() {
        // 1. Definimos qué estados son visibles para el público
        // (Excluimos explícitamente "BORRADOR")
        List<String> estadosVisibles = List.of(
                "INSCRIPCIONES_ABIERTAS",
                "INSCRIPCIONES_CERRADAS",
                "EN_PROGRESO",
                "FINALIZADO"
        );

        // 2. Buscamos solo esos torneos en la BD
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

    // HELPER PARA CONVERTIR FECHAS (Aquí estaba el error)
    private LocalDate convertirFecha(java.util.Date fecha) {
        if (fecha == null) return null;

        // Si es java.sql.Date (viene de BD), usamos su método directo
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }

        // Si es java.util.Date normal, usamos la conversión estándar
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private TorneoPublicoDTO mapToDTO(Torneo t) {
        // 1. Convertimos fechas usando el método seguro
        LocalDate fechaInicio = convertirFecha(t.getFechaInicio());
        LocalDate fechaFin = convertirFecha(t.getFechaFin());

        // 2. Manejo seguro de Categorías
        List<String> categorias = new ArrayList<>();
        if (t.getCategorias() != null) {
            categorias = t.getCategorias().stream()
                    .map(ct -> ct.getCategoria() != null ? ct.getCategoria().name() : "Sin nombre")
                    .collect(Collectors.toList());
        }

        // 3. Estado
        String estadoStr = (t.getEstado() != null) ? t.getEstado().toString() : "DESCONOCIDO";

        // 4. Descripción
        String descripcion = (t.getDescripcion() != null) ? t.getDescripcion() : "Sin descripción";

        return new TorneoPublicoDTO(
                t.getIdTorneo(),
                t.getNombre(),
                fechaInicio,
                fechaFin,
                estadoStr,
                descripcion,
                categorias
        );
    }
}