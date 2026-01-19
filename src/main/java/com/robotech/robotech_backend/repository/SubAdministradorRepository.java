package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EstadoSubAdmin;
import com.robotech.robotech_backend.model.SubAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SubAdministradorRepository
        extends JpaRepository<SubAdministrador, String> {

    boolean existsByUsuario_IdUsuario(String idUsuario);

    Optional<SubAdministrador> findByUsuario_IdUsuario(String idUsuario);

    List<SubAdministrador> findByEstado(EstadoSubAdmin estado);
}

