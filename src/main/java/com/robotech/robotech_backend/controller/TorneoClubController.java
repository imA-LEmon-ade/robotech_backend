package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/club/torneos")
@PreAuthorize("hasRole('CLUB')")
public class TorneoClubController {

    private final TorneoService torneoService;

    // --------------------------------------------------
    // LISTAR TORNEOS DISPONIBLES PARA CLUB
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
    @GetMapping("/{idTorneo}/categorias")
    public ResponseEntity<List<CategoriaTorneo>> categorias(
            @PathVariable String idTorneo
    ) {
        return ResponseEntity.ok(
                torneoService.listarCategorias(idTorneo)
        );
    }
}

