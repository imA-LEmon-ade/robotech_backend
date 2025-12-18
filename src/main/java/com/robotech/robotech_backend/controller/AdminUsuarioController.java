package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.service.AdminUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usuarios")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final AdminUsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable String id, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.editar(id, dto));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestBody String nuevoEstado) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, nuevoEstado));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(@PathVariable String id, @RequestBody String nuevaPass) {
        return ResponseEntity.ok(usuarioService.cambiarPassword(id, nuevaPass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
