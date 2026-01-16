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

    @GetMapping("/{tipo}/categoria/{idCategoriaTorneo}")
    public ResponseEntity<List<RankingDTO>> rankingPorCategoria(
            @PathVariable TipoParticipante tipo,
            @PathVariable String idCategoriaTorneo
    ) {
        return ResponseEntity.ok(
                rankingService.obtenerRanking(tipo, idCategoriaTorneo)
        );
    }
}

