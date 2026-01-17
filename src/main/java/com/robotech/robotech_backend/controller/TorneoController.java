package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarEstadoTorneoDTO;
import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp; // ⚠️ IMPORTANTE: Necesario para que funcione 'editar'

@RestController
@RequestMapping("/api/admin/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
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
    public ResponseEntity<?> listar() {
        // Mantenemos tu log para depuración si lo deseas
        System.out.println("🔥 LISTANDO TODOS LOS TORNEOS");
        return ResponseEntity.ok(torneoService.listar());
    }

    // 3. Editar torneo (AQUÍ ESTABA EL ERROR, YA CORREGIDO)
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable String id,
            @RequestBody CrearTorneoDTO dto
    ) {
        // Convertimos LocalDateTime (del DTO) a Timestamp (de la Entidad)
        Torneo data = Torneo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio() != null ? Timestamp.valueOf(dto.getFechaInicio()) : null)
                .fechaFin(dto.getFechaFin() != null ? Timestamp.valueOf(dto.getFechaFin()) : null)
                .fechaAperturaInscripcion(dto.getFechaAperturaInscripcion() != null ? Timestamp.valueOf(dto.getFechaAperturaInscripcion()) : null)
                .fechaCierreInscripcion(dto.getFechaCierreInscripcion() != null ? Timestamp.valueOf(dto.getFechaCierreInscripcion()) : null)
                .estado(dto.getEstado()) // Agregamos estado por si se edita desde aquí
                .build();

        return ResponseEntity.ok(torneoService.editar(id, data));
    }

    // 4. Cambiar estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable String id,
            @RequestBody CambiarEstadoTorneoDTO dto
    ) {
        return ResponseEntity.ok(
                torneoService.cambiarEstado(id, dto.getEstado())
        );
    }

    // 5. Eliminar torneo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        torneoService.eliminar(id);
        return ResponseEntity.ok("Torneo eliminado");
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