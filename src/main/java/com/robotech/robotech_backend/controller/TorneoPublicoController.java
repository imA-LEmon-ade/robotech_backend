package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.TorneoPublicoDTO;
import com.robotech.robotech_backend.dto.CategoriaTorneoPublicoDTO;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TorneoPublicoController {

    private final TorneoPublicoService service;
    private final CategoriaTorneoService categoriaTorneoService;

    // LISTAR TODOS -> Llama a obtenerTodos() sin argumentos
    @GetMapping
    public ResponseEntity<List<TorneoPublicoDTO>> listar() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    // VER DETALLE -> Llama a obtenerPorId(id) con argumento
    @GetMapping("/{id}")
    public ResponseEntity<TorneoPublicoDTO> detalle(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // LISTAR CATEGORÍAS DE UN TORNEO (PÚBLICO)
    @GetMapping("/{id}/categorias")
    public ResponseEntity<List<CategoriaTorneoPublicoDTO>> listarCategorias(@PathVariable String id) {
        return ResponseEntity.ok(categoriaTorneoService.listarPublicoPorTorneo(id));
    }
}


