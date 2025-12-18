package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Juez;

import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;

import com.robotech.robotech_backend.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final ClubRepository clubRepo;
    private final CompetidorRepository competidorRepo;
    private final JuezRepository juezRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Map<String, Object> login(String correo, String contrasena) {

        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        Map<String, Object> response = new HashMap<>();

        String token = jwtService.generarToken(usuario);
        response.put("token", token);

        response.put("usuario", usuario);
        response.put("rol", usuario.getRol());

        // --- Buscar entidad según el rol ---
        switch (usuario.getRol()) {

            case "CLUB" -> {
                Club club = clubRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Club no encontrado"));
                response.put("entidad", club);
            }

            case "COMPETIDOR" -> {
                Competidor c = competidorRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));
                response.put("entidad", c);
            }

            case "JUEZ" -> {
                Juez j = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Juez no encontrado"));
                response.put("entidad", j);
            }

            case "SUBADMINISTRADOR", "ADMINISTRADOR" -> {
                response.put("entidad", usuario);
            }
        }

        return response;
    }
}
