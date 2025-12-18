package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.SubAdministrador;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.SubAdministradorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubAdministradorService {

    private final SubAdministradorRepository subAdministradorRepository;
    private final UsuarioRepository usuarioRepository;

    // Crear Subadmin
    public SubAdministrador crearSubadmin(String idUsuario, SubAdministrador datos) {

        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("El usuario no existe: " + idUsuario));

        // Validar que el usuario no tenga ya un subadmin asignado
        if (subAdministradorRepository.existsByUsuario_IdUsuario(idUsuario)) {
            throw new RuntimeException("Este usuario ya es Subadministrador");
        }

        datos.setUsuario(usuario);
        return subAdministradorRepository.save(datos);
    }

    // Obtener por ID del Subadmin
    public Optional<SubAdministrador> obtenerPorId(String idSubadmin) {
        return subAdministradorRepository.findById(idSubadmin);
    }

    // Obtener por ID de Usuario
    public Optional<SubAdministrador> obtenerPorUsuario(String idUsuario) {
        return subAdministradorRepository.findByUsuario_IdUsuario(idUsuario);
    }

    // Listar por estado
    public List<SubAdministrador> listarPorEstado(String estado) {
        return subAdministradorRepository.findByEstado(estado);
    }

    // Actualizar datos del subadmin
    public SubAdministrador actualizar(String idSubadmin, SubAdministrador cambios) {
        SubAdministrador subadmin = subAdministradorRepository.findById(idSubadmin)
                .orElseThrow(() -> new RuntimeException("Subadmin no encontrado"));

        subadmin.setDescripcionCargo(cambios.getDescripcionCargo());
        subadmin.setEstado(cambios.getEstado());
        subadmin.setCreadoPor(cambios.getCreadoPor());

        return subAdministradorRepository.save(subadmin);
    }

    // Eliminar subadmin
    public void eliminar(String idSubadmin) {
        if (!subAdministradorRepository.existsById(idSubadmin)) {
            throw new RuntimeException("El Subadministrador no existe");
        }
        subAdministradorRepository.deleteById(idSubadmin);
    }
}
