package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionDTO;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InscripcionController {

    private final InscripcionTorneoService inscripcionService;

    // Competidor se inscribe a un torneo
    @PostMapping("/{id}/inscribir")
    public ResponseEntity<?> inscribir(
            @PathVariable String id,                // idCategoriaTorneo
            @RequestBody InscripcionDTO dto        // contiene idRobot
    ) {
        return ResponseEntity.ok(
                inscripcionService.inscribir(id, dto.getIdRobot())
        );
    }

    // Listar inscritos de un torneo
    @GetMapping("/{id}/inscritos")
    public ResponseEntity<?> inscritos(@PathVariable String id) {
        return ResponseEntity.ok(inscripcionService.listarInscritos(id));
    }
}
