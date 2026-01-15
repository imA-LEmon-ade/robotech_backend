package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    // =========================
    // LISTAR
    // =========================
    public List<UsuarioDTO> listar() {
        return usuarioRepo.findAll()
                .stream()
                .map(u -> new UsuarioDTO(
                        u.getIdUsuario(),
                        u.getNombres(),
                        u.getApellidos(),
                        u.getCorreo(),
                        u.getRol(),
                        u.getEstado(),
                        u.getTelefono()
                ))
                .toList();
    }


    // =========================
    // CREAR
    // =========================
    public Usuario crear(CrearUsuarioDTO dto) {

        if (usuarioRepo.existsByCorreo(dto.correo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.telefono())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }


        Usuario u = Usuario.builder()
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                .rol(RolUsuario.COMPETIDOR) // o el que corresponda
                .estado(EstadoUsuario.ACTIVO)
                .build();

        return usuarioRepo.save(u);
    }



    // =========================
    // EDITAR (SIN PASSWORD)
    // =========================
    public Usuario editar(String id, UsuarioDTO dto) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setCorreo(dto.correo());
        u.setTelefono(dto.telefono());
        u.setNombres(dto.nombres().trim());
        u.setApellidos(dto.apellidos().trim());
        u.setRol(dto.rol());
        u.setEstado(dto.estado());

        return usuarioRepo.save(u);
    }



    // =========================
    // CAMBIAR ESTADO (ACTIVO / INACTIVO)
    // =========================
    public Usuario cambiarEstado(String id, String nuevoEstado) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setEstado(EstadoUsuario.valueOf(nuevoEstado));
        return usuarioRepo.save(u);
    }

    // =========================
    // CAMBIAR CONTRASEÑA
    // =========================
    public Usuario cambiarPassword(String id, String nuevaPass) {

        if (nuevaPass == null || nuevaPass.length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setContrasenaHash(passwordEncoder.encode(nuevaPass));
        return usuarioRepo.save(u);
    }

    // =========================
    // "ELIMINAR" → DESACTIVAR
    // =========================
    public void eliminar(String id) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminación lógica para evitar errores FK
        u.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepo.save(u);
    }
}
