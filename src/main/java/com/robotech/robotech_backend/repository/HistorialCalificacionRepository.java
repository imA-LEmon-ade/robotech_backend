package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.dto.RankingDTO;
import com.robotech.robotech_backend.dto.ResultadoTorneoDTO;
import com.robotech.robotech_backend.model.entity.HistorialCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialCalificacionRepository extends JpaRepository<HistorialCalificacion, String> {

    // 1. RANKING DE ROBOTS
    @Query("""
        SELECT new com.robotech.robotech_backend.dto.RankingDTO(
            r.idRobot, 
            r.nombre,
            CAST((SELECT COUNT(e) FROM Encuentro e WHERE e.ganadorIdReferencia = r.idRobot) AS int),
            0,
            0,
            AVG(CAST(hc.puntaje AS double)),
            CAST(SUM(CAST(hc.puntaje AS int)) AS int)
        )
        FROM HistorialCalificacion hc
        JOIN Robot r ON hc.idReferencia = r.idRobot
        GROUP BY r.idRobot, r.nombre
        ORDER BY SUM(hc.puntaje) DESC
    """)
    List<RankingDTO> obtenerRankingGlobalRobots();

    // 2. RANKING DE COMPETIDORES
    @Query("""
        SELECT new com.robotech.robotech_backend.dto.RankingDTO(
            c.idCompetidor,
            CONCAT(u.nombres, ' ', u.apellidos),
            CAST((SELECT COUNT(e) FROM Encuentro e WHERE e.ganadorIdReferencia IN (SELECT rob.idRobot FROM Robot rob WHERE rob.competidor = c)) AS int),
            0,
            0,
            AVG(CAST(hc.puntaje AS double)),
            CAST(SUM(CAST(hc.puntaje AS int)) AS int)
        )
        FROM HistorialCalificacion hc
        JOIN Robot r ON hc.idReferencia = r.idRobot
        JOIN Competidor c ON r.competidor = c
        JOIN Usuario u ON c.usuario = u
        GROUP BY c.idCompetidor, u.nombres, u.apellidos
        ORDER BY SUM(hc.puntaje) DESC
    """)
    List<RankingDTO> obtenerRankingGlobalCompetidores();

    // 3. RANKING DE CLUBES
    @Query("""
        SELECT new com.robotech.robotech_backend.dto.RankingDTO(
            cl.idClub,
            cl.nombre,
            CAST((SELECT COUNT(e) FROM Encuentro e WHERE e.ganadorIdReferencia IN (SELECT rob.idRobot FROM Robot rob JOIN rob.competidor comp WHERE comp.clubActual = cl)) AS int),
            0,
            0,
            AVG(CAST(hc.puntaje AS double)),
            CAST(SUM(CAST(hc.puntaje AS int)) AS int)
        )
        FROM HistorialCalificacion hc
        JOIN Robot r ON hc.idReferencia = r.idRobot
        JOIN Competidor c ON r.competidor = c
        JOIN Club cl ON c.clubActual = cl
        GROUP BY cl.idClub, cl.nombre
        ORDER BY SUM(hc.puntaje) DESC
    """)
    List<RankingDTO> obtenerRankingGlobalClubes();

    @Query("""
        SELECT new com.robotech.robotech_backend.dto.ResultadoTorneoDTO(r.nombre, SUM(CAST(hc.puntaje AS long)))
        FROM HistorialCalificacion hc
        JOIN Robot r ON hc.idReferencia = r.idRobot
        WHERE hc.encuentro.idEncuentro IN (
            SELECT e.idEncuentro 
            FROM Encuentro e 
            WHERE e.categoriaTorneo.torneo.idTorneo = :idTorneo
        )
        GROUP BY r.nombre
        ORDER BY SUM(hc.puntaje) DESC
    """)
    List<ResultadoTorneoDTO> obtenerRankingRobots(@Param("idTorneo") String idTorneo);

    List<HistorialCalificacion> findByEncuentro_CategoriaTorneo_IdCategoriaTorneo(String idCategoriaTorneo);
}

