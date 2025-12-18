package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/competidor/torneos")
@PreAuthorize("hasRole('COMPETIDOR')")
public class TorneoCompetidorController {

    private final TorneoService torneoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(torneoService.listarParaCompetidor());
    }

    @GetMapping("/{id}/categorias")
    public ResponseEntity<?> categorias(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.listarCategorias(id));
    }
}

