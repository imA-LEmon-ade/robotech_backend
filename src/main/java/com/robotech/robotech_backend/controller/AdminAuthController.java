package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.LoginAdminRequest;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UsuarioRepository usuarioRepo;

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginAdminRequest req) {

        Usuario usuario = usuarioRepo.findByCorreoAndContrasenaHash(
                req.getCorreo(), req.getContrasena()
        ).orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        // Validar roles permitidos SOLO para este login
        if (!usuario.getRol().equals("ADMINISTRADOR") &&
                !usuario.getRol().equals("SUBADMINISTRADOR")) {

            throw new RuntimeException("No autorizado");
        }

        return ResponseEntity.ok(Map.of(
                "usuario", usuario,
                "rol", usuario.getRol()
        ));
    }
}

