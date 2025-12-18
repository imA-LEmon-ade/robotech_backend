package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.CategoriaTorneo;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/club/torneos")
@PreAuthorize("hasRole('CLUB')")
public class TorneoClubController {

    private final TorneoService torneoService;

    @GetMapping("/disponibles")
    public ResponseEntity<?> listarParaClub(Authentication auth) {
        return ResponseEntity.ok(torneoService.listarParaClub(auth));
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(torneoService.listarParaClub());
    }

    // 1️⃣ Categorías del torneo
    @GetMapping("/{idTorneo}/categorias")
    public List<CategoriaTorneo> categorias(@PathVariable String idTorneo) {
        return torneoService.listarCategorias(idTorneo);
    }
}

