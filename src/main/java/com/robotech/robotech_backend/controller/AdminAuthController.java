package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.LoginAdminRequest;
import com.robotech.robotech_backend.model.RolUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.security.JwtService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginAdminRequest req) {
        if (req.getCorreo() == null || req.getCorreo().isBlank()
                || req.getContrasena() == null || req.getContrasena().isBlank()) {
            return ResponseEntity.badRequest().body("Por favor rellena los campos");
        }

        Usuario usuario = usuarioRepo.findByCorreo(req.getCorreo()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(req.getContrasena(), usuario.getContrasenaHash())) {
            return ResponseEntity.status(401).body("Contrasena incorrecta");
        }

        // ✅ VALIDACIÓN CORRECTA CON ENUM
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR &&
                usuario.getRol() != RolUsuario.SUBADMINISTRADOR) {

            return ResponseEntity.status(403)
                    .body("No tienes permisos de administrador");
        }

        String token = jwtService.generarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "usuario", usuario,
                "rol", usuario.getRol(),
                "token", token
        ));
    }
}
