package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.service.EquipoInscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/inscripciones/equipos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipoInscripcionController {

    private final EquipoInscripcionService service;

    @PostMapping
    public ResponseEntity<?> inscribir(
            @RequestHeader("club-id") String clubId,
            @RequestBody EquipoInscripcionDTO dto
    ) {
        return ResponseEntity.ok(
                service.inscribirEquipo(clubId, dto)
        );
    }
}
