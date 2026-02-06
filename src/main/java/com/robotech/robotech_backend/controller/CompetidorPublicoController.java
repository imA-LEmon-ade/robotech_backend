package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.service.CompetidorPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/competidores")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompetidorPublicoController {

    private final CompetidorPublicoService competidorService;

    @GetMapping
    public ResponseEntity<List<CompetidorPublicoDTO>> obtenerRanking() {
        return ResponseEntity.ok(competidorService.obtenerRanking());
    }
}

