package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.model.enums.EstadoInscripcion;
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

    List<InscripcionTorneo> findByRobotCompetidorClubActualUsuarioIdUsuario(String idUsuario);

    List<InscripcionTorneo> findByRobotCompetidorClubActualIdClub(String idClub);

    boolean existsByRobotCompetidorClubActualIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
            String idClub,
            String idTorneo,
            EstadoInscripcion estado
    );

    List<InscripcionTorneo>
    findByRobotCompetidorClubActualUsuarioIdUsuarioAndEstado(
            String idUsuarioClub,
            EstadoInscripcion estado
    );

    // =========================
    // VISTA COMPETIDOR
    // =========================

    // ✅ NUEVO: Trae todas las inscripciones del competidor (sin importar el estado)
    // Esto permitirá que el filtro de "ANULADA" funcione en la vista de competidor
    List<InscripcionTorneo> findByRobotCompetidorUsuarioIdUsuario(String idUsuario);

    // Mantenemos el que ya tenías para no romper otras llamadas
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


