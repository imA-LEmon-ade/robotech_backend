package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.dto.CambiarContrasenaDTO;
import com.robotech.robotech_backend.dto.EditarUsuarioDTO;
import com.robotech.robotech_backend.dto.UsuarioDTO;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final DniValidator dniValidator;
    private final TelefonoValidator telefonoValidator;

    // =========================
    // LISTAR
    // =========================
    public Page<UsuarioDTO> listar(Pageable pageable, String q) {
        Page<Usuario> page = (q == null || q.isBlank())
                ? usuarioRepo.findAll(pageable)
                : usuarioRepo.buscar(q.trim(), pageable);

        return page.map(u -> new UsuarioDTO(
                u.getIdUsuario(),
                u.getDni(),
                u.getNombres(),
                u.getApellidos(),
                u.getCorreo(),
                u.getRoles(),
                u.getEstado(),
                u.getTelefono()
        ));
    }


    // =========================
    // CREAR
    // =========================
    public Usuario crear(CrearUsuarioDTO dto) {

        dniValidator.validar(dto.dni());
        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            telefonoValidator.validar(dto.telefono());
        }

        if (usuarioRepo.existsByCorreo(dto.correo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.telefono())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }


        Usuario u = Usuario.builder()
                .dni(dto.dni())
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                .roles(Set.of(RolUsuario.COMPETIDOR)) // o el que corresponda
                .estado(EstadoUsuario.ACTIVO)
                .build();

        return usuarioRepo.save(u);
    }



    // =========================
    // EDITAR (SIN PASSWORD)
    // =========================
    public Usuario editar(String id, EditarUsuarioDTO dto) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            telefonoValidator.validar(dto.telefono());
        }

        u.setCorreo(dto.correo());
        u.setTelefono(dto.telefono());
        u.setNombres(dto.nombres().trim());
        u.setApellidos(dto.apellidos().trim());
        if (dto.roles() != null) {
        u.setRoles(dto.roles());
        }
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
    // CAMBIAR CONTRASEÃ‘A (ADMIN)
    // =========================
    public Usuario cambiarContrasena(String id, CambiarContrasenaDTO dto) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.contrasenaActual(), u.getContrasenaHash())) {
            throw new RuntimeException("Contrasena actual incorrecta");
        }

        u.setContrasenaHash(passwordEncoder.encode(dto.nuevaContrasena()));
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


