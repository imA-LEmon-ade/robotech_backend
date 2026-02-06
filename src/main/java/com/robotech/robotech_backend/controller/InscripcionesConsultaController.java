package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionResumenDTO;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.InscripcionesConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
public class InscripcionesConsultaController {

    private final InscripcionesConsultaService service;

    // --------------------------------------------------
    // VISTA CLUB
    // --------------------------------------------------
    @GetMapping("/club")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> verInscripcionesClub(
            Authentication auth,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(
                service.listarInscripcionesClub(usuario.getIdUsuario(), busqueda, estado)
        );
    }

    // --------------------------------------------------
    // VISTA COMPETIDOR (CORREGIDA)
    // --------------------------------------------------
    @GetMapping("/competidor")
    @PreAuthorize("hasAuthority('COMPETIDOR')")
    public ResponseEntity<?> verInscripcionesCompetidor(
            Authentication auth,
            @RequestParam(required = false) String busqueda, // ✨ Recibe búsqueda del competidor
            @RequestParam(required = false) String estado    // ✨ Recibe filtro (ACTIVA/ANULADA)
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        // Enviamos los filtros al service para procesarlos en memoria
        return ResponseEntity.ok(
                service.listarInscripcionesCompetidor(usuario.getIdUsuario(), busqueda, estado)
        );
    }

    // --------------------------------------------------
    // VISTA ADMIN
    // --------------------------------------------------
    @GetMapping("/todas")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<?> verTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }
}

