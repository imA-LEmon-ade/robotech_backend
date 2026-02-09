package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CompetidorPublicoDTO;
import com.robotech.robotech_backend.dto.PageResponse;
import com.robotech.robotech_backend.service.CompetidorPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/competidores")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url}")
public class CompetidorPublicoController {

    private final CompetidorPublicoService competidorService;

    @GetMapping
    public ResponseEntity<PageResponse<CompetidorPublicoDTO>> obtenerRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 60);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("usuario.nombres").ascending().and(Sort.by("usuario.apellidos").ascending())
        );
        return ResponseEntity.ok(competidorService.obtenerRanking(pageable, q));
    }
}

