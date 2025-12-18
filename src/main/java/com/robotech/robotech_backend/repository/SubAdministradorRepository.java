package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.SubAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SubAdministradorRepository extends JpaRepository<SubAdministrador, String> {

    // Buscar subadmin por el usuario asociado
    Optional<SubAdministrador> findByUsuario_IdUsuario(String idUsuario);

    // Verificar si ya existe un subadmin vinculado a un usuario
    boolean existsByUsuario_IdUsuario(String idUsuario);

    // Lista por estado (ACTIVO, INACTIVO, SUSPENDIDO)
    List<SubAdministrador> findByEstado(String estado);
}
