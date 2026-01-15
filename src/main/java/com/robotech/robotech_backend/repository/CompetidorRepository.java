package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Competidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.*;

public interface CompetidorRepository extends JpaRepository<Competidor, String> {
    boolean existsByUsuario_IdUsuario(String idUsuario);
    List<Competidor> findByClubActual_IdClub(String idClub);
    Optional<Competidor> findByUsuario_IdUsuario(String idUsuario);
    boolean existsByDni(String dni);
    long countByClubActual_IdClub(String idClub);
}
