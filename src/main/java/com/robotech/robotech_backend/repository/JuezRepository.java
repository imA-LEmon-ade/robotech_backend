package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Juez;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JuezRepository extends JpaRepository<Juez, String> {
    boolean existsByUsuario_IdUsuario(String idUsuario);

    Optional<Juez> findByUsuario_IdUsuario(String idUsuario);
}
