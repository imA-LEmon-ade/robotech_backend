package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
import com.robotech.robotech_backend.service.EquipoInscripcionService;
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
    private final EquipoInscripcionService equipoService;
    private final InscripcionesConsultaService consultaService;

    // Listar todas (admin)
    @GetMapping
    public List<InscripcionResumenDTO> listarTodas() {
        return consultaService.listarTodas();
    }

    // Anular inscripcion individual
    @PutMapping("/{id}/anular")
    public ResponseEntity<?> anular(
            @PathVariable String id,
            @RequestBody(required = false) String motivo
    ) {
        inscripcionService.anularInscripcion(
                id,
                motivo != null ? motivo : "Anulacion administrativa"
        );
        return ResponseEntity.ok("Inscripcion anulada");
    }

    // Anular inscripcion por equipo
    @PutMapping("/equipo/{id}/anular")
    public ResponseEntity<?> anularEquipo(
            @PathVariable String id,
            @RequestBody(required = false) String motivo
    ) {
        equipoService.anularEquipo(
                id,
                motivo != null ? motivo : "Anulacion administrativa"
        );
        return ResponseEntity.ok("Inscripcion de equipo anulada");
    }
}
