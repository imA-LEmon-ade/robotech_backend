package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.JuezDTO;
import com.robotech.robotech_backend.dto.JuezSelectDTO;
import com.robotech.robotech_backend.service.AdminJuezService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jueces")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminJuezController {

    private final AdminJuezService juezService;

    // CRUD NORMAL
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody JuezDTO dto) {
        return ResponseEntity.ok(juezService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(juezService.listar());
    }

    // ✅ ENDPOINT PARA SELECT
    @GetMapping("/select")
    public List<JuezSelectDTO> listarParaSelect() {
        return juezService.listarJuecesParaSelect();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable String id,
            @RequestBody JuezDTO dto
    ) {
        return ResponseEntity.ok(juezService.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        juezService.eliminar(id);
        return ResponseEntity.ok("Juez eliminado");
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(
            @PathVariable String id,
            @RequestHeader("admin-id") String adminId
    ) {
        return ResponseEntity.ok(juezService.aprobar(id, adminId));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(
            @PathVariable String id,
            @RequestHeader("admin-id") String adminId
    ) {
        return ResponseEntity.ok(juezService.rechazar(id, adminId));
    }
}
