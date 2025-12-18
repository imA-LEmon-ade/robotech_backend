package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.SubAdministrador;
import com.robotech.robotech_backend.service.SubAdministradorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subadmin")
@RequiredArgsConstructor
public class SubAdministradorController {

    private final SubAdministradorService subadminService;

    // Crear subadmin
    @PostMapping("/crear/{idUsuario}")
    public ResponseEntity<?> crearSubadmin(
            @PathVariable String idUsuario,
            @RequestBody SubAdministrador body
    ) {
        return ResponseEntity.ok(subadminService.crearSubadmin(idUsuario, body));
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(subadminService.obtenerPorId(id));
    }

    // Obtener por usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> obtenerPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(subadminService.obtenerPorUsuario(idUsuario));
    }

    // Listar por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(subadminService.listarPorEstado(estado));
    }

    // Actualizar subadmin
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody SubAdministrador body
    ) {
        return ResponseEntity.ok(subadminService.actualizar(id, body));
    }

    // Eliminar
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        subadminService.eliminar(id);
        return ResponseEntity.ok("Subadministrador eliminado");
    }
}
