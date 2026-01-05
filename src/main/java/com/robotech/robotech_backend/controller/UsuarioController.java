package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.security.JwtService;
import com.robotech.robotech_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {

        if (usuarioService.correoExiste(usuario.getCorreo())) {
            return ResponseEntity.badRequest().body("Correo ya registrado");
        }

        if (usuarioService.telefonoExiste(usuario.getTelefono())) {
            return ResponseEntity.badRequest().body("Teléfono ya registrado");
        }

        return ResponseEntity.ok(usuarioService.crearUsuario(usuario));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {

        String correo = data.get("correo");
        String contrasena = data.get("contrasena");

        return usuarioService.login(correo, contrasena)
                .map(usuario -> {

                    String token = jwtService.generarToken(usuario);

                    return ResponseEntity.ok(
                            Map.of(
                                    "token", token,
                                    "idUsuario", usuario.getIdUsuario(),
                                    "rol", usuario.getRol(),
                                    "correo", usuario.getCorreo()
                            )
                    );
                })
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of("error", "Credenciales inválidas")
                        ));
    }


}
