package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.LoginAdminRequest;
import com.robotech.robotech_backend.model.RolUsuario; // ⚠️ IMPORTANTE: Importar el Enum
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

        // 1. Buscar usuario
        Usuario usuario = usuarioRepo.findByCorreo(req.getCorreo())
                .orElse(null); // No lanzamos excepción aquí para manejarlo con ResponseEntity abajo

        // 2. Validar que exista y la contraseña coincida
        if (usuario == null || !passwordEncoder.matches(req.getContrasena(), usuario.getContrasenaHash())) {
            // Devolvemos 401 (Unauthorized) en lugar de error 500
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        // 3. Validar roles usando el ENUM (Corregido)
        // Usamos != para comparar Enums. NO uses .equals("TEXTO")
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR &&
                usuario.getRol() != RolUsuario.SUBADMINISTRADOR) {

            // Devolvemos 403 (Forbidden) porque sí existe pero no tiene permisos
            return ResponseEntity.status(403).body("No tienes permisos de administrador");
        }

        // 4. Generar Token
        String token = jwtService.generarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "usuario", usuario,
                "rol", usuario.getRol(),
                "token", token
        ));
    }
}