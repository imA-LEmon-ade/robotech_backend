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

        Usuario usuario = usuarioRepo.findByCorreo(req.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.getContrasena(), usuario.getContrasenaHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        // Validar roles permitidos SOLO para este login

        if (usuario.getRol() != RolUsuario.ADMINISTRADOR &&
                usuario.getRol() != RolUsuario.SUBADMINISTRADOR) {

            throw new RuntimeException("No autorizado");
        }

        String token = jwtService.generarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "usuario", usuario,
                "rol", usuario.getRol(),
                "token", token
        ));
    }
}

