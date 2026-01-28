package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EquipoTorneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoTorneoRepository
        extends JpaRepository<EquipoTorneo, String> {

    // Equipos inscritos por categoría
    long countByCategoriaTorneoIdCategoriaTorneo(String idCategoria);

    long countByCategoriaTorneoIdCategoriaTorneoAndEstadoNot(
            String idCategoria,
            com.robotech.robotech_backend.model.EstadoEquipoTorneo estado
    );

    // Ver si un robot ya está inscrito en este torneo
    boolean existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneo(
            String idRobot,
            String idTorneo
    );

    boolean existsByRobotsIdRobotAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
            String idRobot,
            String idTorneo,
            com.robotech.robotech_backend.model.EstadoEquipoTorneo estado
    );

    boolean existsByNombreIgnoreCaseAndCategoriaTorneoIdCategoriaTorneo(
            String nombre,
            String idCategoriaTorneo
    );


    // Listar equipos de un club en un torneo
    List<EquipoTorneo> findByClubIdClubAndCategoriaTorneoTorneoIdTorneo(
            String idClub,
            String idTorneo
    );

    boolean existsByClubIdClubAndCategoriaTorneoTorneoIdTorneoAndEstadoNot(
            String idClub,
            String idTorneo,
            com.robotech.robotech_backend.model.EstadoEquipoTorneo estado
    );

    // 🔹 Vista CLUB
    List<EquipoTorneo> findByClubUsuarioIdUsuario(String idUsuarioClub);

    // 🔹 Vista COMPETIDOR
    List<EquipoTorneo> findByRobotsCompetidorUsuarioIdUsuario(String idUsuario);

    List<EquipoTorneo> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

}
