package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
import com.robotech.robotech_backend.model.InscripcionTorneo;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import com.robotech.robotech_backend.service.InscripcionesConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminInscripcionController {

    private final InscripcionTorneoService inscripcionService;
    private final InscripcionesConsultaService consultaService;

    // ✅ LISTAR TODAS (ADMIN)
    @GetMapping
    public List<InscripcionResumenDTO> listarTodas() {
        return consultaService.listarTodas();
    }

    // ❌ ANULAR INSCRIPCIÓN
    @PutMapping("/{id}/anular")
    public ResponseEntity<?> anular(
            @PathVariable String id,
            @RequestBody(required = false) String motivo
    ) {
        inscripcionService.anularInscripcion(
                id,
                motivo != null ? motivo : "Anulación administrativa"
        );
        return ResponseEntity.ok("Inscripción anulada");
    }


}

