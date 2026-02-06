package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarEstadoTorneoDTO;
import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.model.entity.Torneo;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/torneos")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
public class TorneoController {

    private final TorneoService torneoService;
    private final CategoriaTorneoService categoriaTorneoService;

    // 1. Crear torneo
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearTorneoDTO dto, Authentication auth) {
        return ResponseEntity.ok(torneoService.crearTorneo(dto, auth));
    }

    // 2. Listar torneos
    @GetMapping
    public ResponseEntity<PageResponse<Torneo>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("idTorneo").ascending()
        );

        Page<Torneo> result = torneoService.listar(pageable, q);
        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        ));
    }

    // 3. Editar torneo
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable String id, @RequestBody CrearTorneoDTO dto) {
        // Mapeo manual para asegurar que los tipos LocalDateTime del DTO
        // se conviertan correctamente a los Timestamps de la entidad Torneo
        Torneo data = Torneo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio() != null ? Timestamp.valueOf(dto.getFechaInicio()) : null)
                .fechaFin(dto.getFechaFin() != null ? Timestamp.valueOf(dto.getFechaFin()) : null)
                .fechaAperturaInscripcion(dto.getFechaAperturaInscripcion() != null ? Timestamp.valueOf(dto.getFechaAperturaInscripcion()) : null)
                .fechaCierreInscripcion(dto.getFechaCierreInscripcion() != null ? Timestamp.valueOf(dto.getFechaCierreInscripcion()) : null)
                .estado(dto.getEstado())
                .build();

        return ResponseEntity.ok(torneoService.editar(id, data));
    }

    // 4. Cambiar estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestBody CambiarEstadoTorneoDTO dto) {
        return ResponseEntity.ok(torneoService.cambiarEstado(id, dto.getEstado()));
    }

    // 5. Eliminar torneo (CORREGIDO PARA MEJOR RESPUESTA)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            torneoService.eliminar(id);
            // Retornamos un JSON en lugar de un String simple para que el Front lo maneje mejor
            return ResponseEntity.ok(Map.of("message", "Torneo eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Cerrar inscripciones
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrar(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.cerrarInscripciones(id));
    }

    // 7. Listar categorías del torneo
    @GetMapping("/{idTorneo}/categorias")
    public ResponseEntity<?> listarCategorias(@PathVariable String idTorneo) {
        return ResponseEntity.ok(categoriaTorneoService.listarPorTorneo(idTorneo));
    }

    // 8. Obtener un torneo por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.obtener(id));
    }
}

