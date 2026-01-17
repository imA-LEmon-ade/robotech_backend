package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.RankingDTO;
import com.robotech.robotech_backend.model.TipoParticipante;
import com.robotech.robotech_backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RankingController {

    private final RankingService rankingService;

    // ==========================================================
    // 1. ENDPOINTS GLOBALES (Para la página de Rankings)
    // ==========================================================

    @GetMapping("/robots")
    public ResponseEntity<List<RankingDTO>> rankingGlobalRobots() {
        return ResponseEntity.ok(rankingService.obtenerRankingGlobalRobots());
    }

    @GetMapping("/competidores")
    public ResponseEntity<List<RankingDTO>> rankingGlobalCompetidores() {
        return ResponseEntity.ok(rankingService.obtenerRankingGlobalCompetidores());
    }

    @GetMapping("/clubes")
    public ResponseEntity<List<RankingDTO>> rankingGlobalClubes() {
        return ResponseEntity.ok(rankingService.obtenerRankingGlobalClubes());
    }

    // ==========================================================
    // 2. ENDPOINT ESPECÍFICO (Legacy / Por Torneo)
    // ==========================================================
    @GetMapping("/{tipo}/categoria/{idCategoriaTorneo}")
    public ResponseEntity<List<RankingDTO>> rankingPorCategoria(
            @PathVariable TipoParticipante tipo,
            @PathVariable String idCategoriaTorneo
    ) {
        // ⚠️ Nota: Llamamos al método renombrado en el Service
        return ResponseEntity.ok(
                rankingService.obtenerRankingPorCategoria(tipo, idCategoriaTorneo)
        );
    }
}