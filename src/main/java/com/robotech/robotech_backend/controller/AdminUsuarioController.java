package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CambiarContrasenaDTO;
import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.EditarUsuarioDTO;
import com.robotech.robotech_backend.service.AdminUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.robotech.robotech_backend.service.UsuarioService;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final AdminUsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(
            @Valid @RequestBody CrearUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarUsuario(
            @PathVariable String id,
            @Valid @RequestBody EditarUsuarioDTO dto
    ) {
        return ResponseEntity.ok(usuarioService.editar(id, dto));
    }

    @PutMapping("/usuarios/{id}/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(
            @PathVariable String id,
            @Valid @RequestBody CambiarContrasenaDTO dto
    ) {
        usuarioService.cambiarContrasena(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}