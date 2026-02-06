package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import com.robotech.robotech_backend.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClubTorneoController {

    private final TorneoService torneoService;
    private final CategoriaTorneoService categoriaTorneoService;

    // Torneos disponibles
    @GetMapping("/disponibles")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<?> listarDisponibles(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(torneoService.listarDisponiblesParaClub(usuario.getIdUsuario()));
    }

    // Categorias de un torneo
    @GetMapping("/{idTorneo}/categorias")
    public ResponseEntity<?> categoriasPorTorneo(@PathVariable String idTorneo) {
        return ResponseEntity.ok(
                categoriaTorneoService.listarPorTorneo(idTorneo)
        );
    }
}


