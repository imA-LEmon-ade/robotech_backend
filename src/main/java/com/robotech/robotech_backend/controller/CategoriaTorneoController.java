package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CategoriaTorneoDTO;
import com.robotech.robotech_backend.service.CategoriaTorneoService;
import lombok.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categorias-torneo")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CategoriaTorneoController {

    private final CategoriaTorneoService categoriaService;

    @GetMapping("/{idTorneo}")
    public ResponseEntity<?> listar(@PathVariable String idTorneo) {
        return ResponseEntity.ok(categoriaService.listarPorTorneo(idTorneo));
    }

    @PostMapping("/{idTorneo}")
    public ResponseEntity<?> crear(@PathVariable String idTorneo, @RequestBody CategoriaTorneoDTO dto) {
        return ResponseEntity.ok(categoriaService.crear(idTorneo, dto));
    }

    @PutMapping("/{idCategoria}")
    public ResponseEntity<?> editar(@PathVariable String idCategoria, @RequestBody CategoriaTorneoDTO dto) {
        return ResponseEntity.ok(categoriaService.editar(idCategoria, dto));
    }

    @DeleteMapping("/{idCategoria}")
    public ResponseEntity<?> eliminar(@PathVariable String idCategoria) {
        String mensaje = categoriaService.eliminar(idCategoria);
        return ResponseEntity.ok(mensaje);
    }
}


