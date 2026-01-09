package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.InscripcionTorneo;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminInscripcionController {

    private final InscripcionTorneoService inscripcionService;

    // ❌ ANULAR INSCRIPCIÓN
    @PutMapping("/{id}/anular")
    public ResponseEntity<?> anular(@PathVariable String id) {
        inscripcionService.anular(id);
        return ResponseEntity.ok("Inscripción anulada");
    }
}

