package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/competidor/torneos")
@PreAuthorize("hasRole('COMPETIDOR')")
public class TorneoCompetidorController {

    private final TorneoService torneoService;

    // --------------------------------------------------
    // LISTAR TORNEOS DISPONIBLES PARA COMPETIDOR
    // --------------------------------------------------
    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(
                torneoService.listarPublicos()
        );
    }

    // --------------------------------------------------
    // LISTAR CATEGORÍAS DE UN TORNEO
    // --------------------------------------------------
    @GetMapping("/{id}/categorias")
    public ResponseEntity<?> categorias(@PathVariable String id) {
        return ResponseEntity.ok(
                torneoService.listarCategorias(id)
        );
    }
}
