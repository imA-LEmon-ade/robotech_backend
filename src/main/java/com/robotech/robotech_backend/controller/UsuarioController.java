package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // -------------------------
    // LISTAR
    // -------------------------
    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarTodos()
                .stream()
                .map(u -> new UsuarioDTO(
                        u.getIdUsuario(),
                        u.getNombres(),
                        u.getApellidos(),
                        u.getCorreo(),
                        u.getTelefono(),
                        u.getRol(),
                        u.getEstado().name()
                ))
                .toList();
    }

    // -------------------------
    // CREAR
    // -------------------------
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(
            @RequestBody CrearUsuarioDTO dto
    ) {

        Usuario u = usuarioService.crearUsuario(dto);

        return ResponseEntity.ok(
                new UsuarioDTO(
                        u.getIdUsuario(),
                        u.getNombres(),
                        u.getApellidos(),
                        u.getCorreo(),
                        u.getTelefono(),
                        u.getRol(),
                        u.getEstado().name()
                )
        );
    }
}
