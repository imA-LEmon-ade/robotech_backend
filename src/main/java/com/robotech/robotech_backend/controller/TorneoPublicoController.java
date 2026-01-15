package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.TorneoPublicoDTO;
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
}