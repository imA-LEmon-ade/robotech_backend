package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EquipoInscripcionDTO;
import com.robotech.robotech_backend.service.EquipoInscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/inscripciones/equipos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipoInscripcionController {

    private final EquipoInscripcionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> inscribir(
            @RequestBody EquipoInscripcionDTO dto,
            Authentication auth
    ) {
        if (auth == null || !(auth.getPrincipal() instanceof com.robotech.robotech_backend.model.Usuario usuario)) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        return ResponseEntity.ok(
                service.inscribirEquipoPorUsuario(usuario.getIdUsuario(), dto)
        );
    }
}
