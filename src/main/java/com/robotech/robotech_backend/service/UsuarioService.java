package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------
    // LISTAR
    // -------------------------
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // -------------------------
    // CREAR USUARIO (DESDE DTO)
    // -------------------------
    public Usuario crearUsuario(CrearUsuarioDTO dto) {

        if (dto.nombres() == null || dto.nombres().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dto.apellidos() == null || dto.apellidos().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new IllegalArgumentException("Correo ya registrado");
        }

        if (usuarioRepository.existsByTelefono(dto.telefono())) {
            throw new IllegalArgumentException("Teléfono ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                .rol("ADMINISTRADOR") // o el rol que definas
                .estado(EstadoUsuario.ACTIVO)
                .build();

        return usuarioRepository.save(usuario);
    }

    // -------------------------
    // LOGIN (solo ACTIVO)
    // -------------------------
    public Optional<Usuario> login(String correo, String contrasena) {
        return usuarioRepository.findByCorreo(correo)
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .filter(u -> passwordEncoder.matches(contrasena, u.getContrasenaHash()));
    }

    // -------------------------
    // DESACTIVAR (NO BORRAR)
    // -------------------------
    public boolean eliminarUsuario(String id) {
        return usuarioRepository.findById(id).map(u -> {
            u.setEstado(EstadoUsuario.INACTIVO);
            usuarioRepository.save(u);
            return true;
        }).orElse(false);
    }
}
