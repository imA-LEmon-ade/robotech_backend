package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.model.EstadoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionTorneoRepository
        extends JpaRepository<InscripcionTorneo, String> {

    // =========================
    // GENERALES
    // =========================

    List<InscripcionTorneo> findByCategoriaTorneoIdCategoriaTorneo(
            String idCategoriaTorneo
    );

    List<InscripcionTorneo> findByCategoriaTorneoIdCategoriaTorneoAndEstado(
            String idCategoriaTorneo,
            EstadoInscripcion estado
    );

    boolean existsByRobot_IdRobotAndCategoriaTorneo_Torneo_IdTorneoAndEstado(
            String idRobot,
            String idTorneo,
            EstadoInscripcion estado
    );


    long countByCategoriaTorneo_IdCategoriaTorneoAndEstado(
            String idCategoriaTorneo,
            EstadoInscripcion estado
    );


    // =========================
    // VALIDACIONES
    // =========================

    boolean existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneoAndEstado(
            String idRobot,
            String idTorneo,
            EstadoInscripcion estado
    );

    // =========================
    // CUPOS
    // =========================

    long countByCategoriaTorneoIdCategoriaTorneoAndEstado(
            String idCategoriaTorneo,
            EstadoInscripcion estado
    );

    // =========================
    // VISTA CLUB
    // =========================

    List<InscripcionTorneo>
    findByRobotCompetidorClubActualUsuarioIdUsuarioAndEstado(
            String idUsuarioClub,
            EstadoInscripcion estado
    );

    // =========================
    // VISTA COMPETIDOR
    // =========================

    List<InscripcionTorneo>
    findByRobotCompetidorUsuarioIdUsuarioAndEstado(
            String idUsuario,
            EstadoInscripcion estado
    );

    // =========================
    // TORNEO
    // =========================

    List<InscripcionTorneo>
    findByCategoriaTorneoTorneoIdTorneo(String idTorneo);
}
