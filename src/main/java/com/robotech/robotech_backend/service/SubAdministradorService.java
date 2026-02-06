package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearSubAdminDTO;
import com.robotech.robotech_backend.dto.EditarSubAdminDTO;
import com.robotech.robotech_backend.dto.SubAdminResponseDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.SubAdministradorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.validadores.DniValidator;
import com.robotech.robotech_backend.service.validadores.TelefonoValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SubAdministradorService {

    private final SubAdministradorRepository subAdminRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final DniValidator dniValidator;
    private final TelefonoValidator telefonoValidator;

    // =========================
    // CREAR SUBADMIN
    // =========================
    public SubAdminResponseDTO crear(CrearSubAdminDTO dto) {

        dniValidator.validar(dto.getDni());
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }

        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 1️⃣ Crear usuario
        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.SUBADMINISTRADOR))
                .estado(EstadoUsuario.ACTIVO)
                .build();

        usuarioRepo.save(usuario);

        // 2️⃣ Crear subadministrador (MapsId)
        SubAdministrador sub = SubAdministrador.builder()
                .usuario(usuario) // 🔥 CLAVE
                .estado(EstadoSubAdmin.ACTIVO)
                .creadoPor("ADMIN")
                .build();

        subAdminRepo.save(sub);

        return map(sub);
    }

    // =========================
    // LISTAR TODOS
    // =========================
    public Page<SubAdminResponseDTO> listarTodos(Pageable pageable, String q) {
        String term = (q == null || q.isBlank()) ? null : q.trim();
        return subAdminRepo.buscar(term, pageable)
                .map(this::map);
    }

    // =========================
    // LISTAR POR ESTADO
    // =========================
    public List<SubAdminResponseDTO> listarPorEstado(EstadoSubAdmin estado) {
        return subAdminRepo.findByEstado(estado)
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // CAMBIAR ESTADO
    // =========================
    public SubAdminResponseDTO cambiarEstado(String id, EstadoSubAdmin estado) {
        SubAdministrador sub = subAdminRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subadmin no encontrado"));

        sub.setEstado(estado);
        subAdminRepo.save(sub);

        return map(sub);
    }



    // =========================
    // MAPPER
    // =========================
    private SubAdminResponseDTO map(SubAdministrador s) {

        Usuario u = s.getUsuario();

        if (u == null) {
            throw new IllegalStateException(
                    "SubAdministrador sin usuario asociado: " + s.getIdUsuario()
            );
        }

        return new SubAdminResponseDTO(
                s.getIdUsuario(),
                u.getNombres(),
                u.getApellidos(),
                u.getCorreo(),
                u.getTelefono(),
                u.getDni(),
                s.getEstado()

        );
    }

    public SubAdminResponseDTO editar(String id, EditarSubAdminDTO dto) {

        SubAdministrador sub = subAdminRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subadmin no encontrado"));

        Usuario usuario = sub.getUsuario();

        if (usuario == null) {
            throw new IllegalStateException("Subadmin sin usuario asociado");
        }

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());

        usuarioRepo.save(usuario); // 🔥 ESTO ERA LO QUE FALTABA

        return map(sub);
    }

}


