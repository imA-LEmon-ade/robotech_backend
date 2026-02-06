package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.ClubPublicoDTO;
import com.robotech.robotech_backend.service.ClubPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/clubes")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}") // Importante para que React pueda conectarse
public class ClubPublicoController {

    private final ClubPublicoService clubPublicoService;

    @GetMapping
    public ResponseEntity<List<ClubPublicoDTO>> listarTodos() {
        return ResponseEntity.ok(clubPublicoService.obtenerClubesParaPublico());
    }
}

