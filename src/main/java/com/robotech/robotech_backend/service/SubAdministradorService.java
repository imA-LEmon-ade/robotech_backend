package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearSubAdminDTO;
import com.robotech.robotech_backend.dto.EditarSubAdminDTO;
import com.robotech.robotech_backend.dto.SubAdminResponseDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.SubAdministradorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubAdministradorService {

    private final SubAdministradorRepository subAdminRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    // =========================
    // CREAR SUBADMIN
    // =========================
    public SubAdminResponseDTO crear(CrearSubAdminDTO dto) {

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
                .rol(RolUsuario.SUBADMINISTRADOR)
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
    public List<SubAdminResponseDTO> listarTodos() {
        return subAdminRepo.findAll()
                .stream()
                .map(this::map)
                .toList();
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

        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());

        usuarioRepo.save(usuario); // 🔥 ESTO ERA LO QUE FALTABA

        return map(sub);
    }

}
