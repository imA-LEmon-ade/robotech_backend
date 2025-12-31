package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarEstadoTorneoDTO;
import com.robotech.robotech_backend.dto.CrearTorneoDTO;
import com.robotech.robotech_backend.dto.InscripcionDTO;
import com.robotech.robotech_backend.model.Torneo;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TorneoController {

    private final TorneoService torneoService;
    private final CategoriaTorneoService categoriaTorneoService;

    // Crear torneo
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearTorneoDTO dto, Authentication auth) {

        Torneo torneo = Torneo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .fechaAperturaInscripcion(dto.getFechaAperturaInscripcion())
                .fechaCierreInscripcion(dto.getFechaCierreInscripcion())
                .estado("BORRADOR")
                .build();

        if (auth != null && auth.getPrincipal() instanceof com.robotech.robotech_backend.model.Usuario usuario) {
            torneo.setCreadoPor(usuario.getIdUsuario());
        }

        return ResponseEntity.ok(torneoService.crearTorneo(torneo));
    }



   // Listar torneos
    @GetMapping
   public ResponseEntity<?> listar(Authentication auth) {
        return ResponseEntity.ok(torneoService.listarPorAdministrador(auth));
    }

    // Editar torneo
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable String id,
            @RequestBody CrearTorneoDTO dto
    ) {

        Torneo data = Torneo.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .fechaAperturaInscripcion(dto.getFechaAperturaInscripcion())
                .fechaCierreInscripcion(dto.getFechaCierreInscripcion())
                .build();

        return ResponseEntity.ok(torneoService.editar(id, data));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable String id,
            @RequestBody CambiarEstadoTorneoDTO dto
    ) {
        return ResponseEntity.ok(
                torneoService.cambiarEstado(id, dto.getEstado())
        );
    }


    // Eliminar torneo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        torneoService.eliminar(id);
        return ResponseEntity.ok("Torneo eliminado");
    }

    // Cerrar inscripciones
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrar(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.cerrarInscripciones(id));
    }

    // Listar categorías del torneo
    @GetMapping("/{idTorneo}/categorias")
    public ResponseEntity<?> listarCategorias(@PathVariable String idTorneo) {
       return ResponseEntity.ok(categoriaTorneoService.listarPorTorneo(idTorneo));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable String id) {
         return ResponseEntity.ok(torneoService.obtener(id));
    }


}
