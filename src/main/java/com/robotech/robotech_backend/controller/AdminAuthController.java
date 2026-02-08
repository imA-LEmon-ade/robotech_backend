package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.LoginAdminRequest;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.security.JwtService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${app.frontend.url}")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginAdminRequest req) {
        boolean correoVacio = req.getCorreo() == null || req.getCorreo().isBlank();
        boolean contrasenaVacia = req.getContrasena() == null || req.getContrasena().isBlank();
        if (correoVacio && contrasenaVacia) {
            return ResponseEntity.badRequest().body("Usuario y contraseña vacíos");
        }
        if (correoVacio) {
            return ResponseEntity.badRequest().body("Usuario vacío");
        }
        if (contrasenaVacia) {
            return ResponseEntity.badRequest().body("Contraseña vacía");
        }

        Usuario usuario = usuarioRepo.findByCorreo(req.getCorreo()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(req.getContrasena(), usuario.getContrasenaHash())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        Set<RolUsuario> roles = usuario.getRoles();
        if (!roles.contains(RolUsuario.ADMINISTRADOR) && !roles.contains(RolUsuario.SUBADMINISTRADOR)) {
            return ResponseEntity.status(403)
                    .body("No tienes permisos de administrador");
        }

        String token = jwtService.generarToken(usuario);

        return ResponseEntity.ok(Map.of(
                "usuario", new UsuarioDTO(
                        usuario.getIdUsuario(),
                        usuario.getDni(),
                        usuario.getNombres(),
                        usuario.getApellidos(),
                        usuario.getCorreo(),
                        usuario.getRoles(),
                        usuario.getEstado(),
                        usuario.getTelefono()
                ),
                "roles", usuario.getRoles(),
                "token", token
        ));
    }
}


