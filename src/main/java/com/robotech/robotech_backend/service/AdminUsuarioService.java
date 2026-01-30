package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CambiarContrasenaDTO;
import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.EditarUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.EstadoUsuario;
import com.robotech.robotech_backend.model.RolUsuario;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.FieldValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                        u.getDni(),
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
            throw new FieldValidationException(
                    "correo",
                    "El correo ya está registrado",
                    List.of("Prueba con otro correo")
            );
        }

        if (usuarioRepo.existsByTelefono(dto.telefono())) {
            throw new FieldValidationException(
                    "telefono",
                    "El teléfono ya está registrado",
                    List.of()
            );
        }

        if (usuarioRepo.existsByDni(dto.dni())) {
            throw new FieldValidationException(
                    "dni",
                    "El DNI ya está registrado",
                    List.of()
            );
        }


        Usuario u = Usuario.builder()
                .dni(dto.dni())
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
    // EDITAR
    // =========================
    public Usuario editar(String id, EditarUsuarioDTO dto) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!u.getCorreo().equals(dto.correo())
                && usuarioRepo.existsByCorreo(dto.correo())) {
            throw new FieldValidationException(
                    "correo",
                    "El correo ya existe",
                    List.of()
            );
        }

        if (!u.getTelefono().equals(dto.telefono())
                && usuarioRepo.existsByTelefono(dto.telefono())) {
            throw new FieldValidationException(
                    "telefono",
                    "El teléfono ya existe",
                    List.of()
            );
        }

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
    @Transactional
    public void cambiarContrasena(String id, CambiarContrasenaDTO dto) {

        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new FieldValidationException(
                    "contrasenaActual",
                    "La contraseña actual es incorrecta"
            );
        }

        usuario.setContrasenaHash(
                passwordEncoder.encode(dto.nuevaContrasena())
        );

        usuarioRepo.save(usuario);
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