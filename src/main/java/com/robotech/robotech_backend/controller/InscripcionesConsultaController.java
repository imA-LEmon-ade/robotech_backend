package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.InscripcionesConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InscripcionesConsultaController {

    private final InscripcionesConsultaService service;

    // --------------------------------------------------
    // VISTA CLUB
    // --------------------------------------------------
    @GetMapping("/club")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> verInscripcionesClub(Authentication auth) {

        Usuario usuario = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(
                service.listarInscripcionesClub(usuario.getIdUsuario())
        );
    }

    // --------------------------------------------------
    // VISTA COMPETIDOR
    // --------------------------------------------------
    @GetMapping("/competidor")
    @PreAuthorize("hasAuthority('COMPETIDOR')")
    public ResponseEntity<?> verInscripcionesCompetidor(Authentication auth) {

        Usuario usuario = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(
                service.listarInscripcionesCompetidor(usuario.getIdUsuario())
        );
    }
}

