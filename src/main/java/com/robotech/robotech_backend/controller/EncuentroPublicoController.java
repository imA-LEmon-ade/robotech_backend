package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.EncuentroPublicoDTO;
import com.robotech.robotech_backend.service.EncuentroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categorias")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EncuentroPublicoController {

    private final EncuentroService encuentroService;

    // LISTAR ENCUENTROS POR CATEGORÍA (PÚBLICO)
    @GetMapping("/{idCategoriaTorneo}/encuentros")
    public ResponseEntity<List<EncuentroPublicoDTO>> listarEncuentros(@PathVariable String idCategoriaTorneo) {
        return ResponseEntity.ok(encuentroService.listarEncuentrosPublicosPorCategoria(idCategoriaTorneo));
    }
}


