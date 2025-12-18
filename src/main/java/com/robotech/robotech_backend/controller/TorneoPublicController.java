package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/torneos")
public class TorneoPublicController {

    private final TorneoService torneoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(torneoService.listarPublicos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.obtener(id));
    }

    @GetMapping("/{id}/categorias")
    public ResponseEntity<?> categorias(@PathVariable String id) {
        return ResponseEntity.ok(torneoService.listarCategorias(id));
    }
}

