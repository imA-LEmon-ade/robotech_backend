package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.enums.EstadoRobot;
import com.robotech.robotech_backend.model.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RobotRepository extends JpaRepository<Robot, String> {

    @Query("SELECT r FROM Robot r JOIN FETCH r.competidor c JOIN FETCH c.usuario u LEFT JOIN FETCH c.clubActual cl")
    List<Robot> findAllWithDetalles();

    List<Robot> findByCompetidor_IdCompetidor(String idCompetidor);

    boolean existsByCompetidor_IdCompetidorAndCategoria(String idCompetidor, CategoriaCompetencia categoria);

    boolean existsByNickname(String nickname);
    boolean existsByNombre(String nombre);

    List<Robot> findByCompetidor_ClubActual(Club club);

    int countByCompetidor_IdCompetidor(String idCompetidor);

    @Query("""
    SELECT r FROM Robot r
    WHERE (:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
      AND (:categoria IS NULL OR r.categoria = :categoria)
      AND (:idClub IS NULL OR r.competidor.clubActual.idClub = :idClub)
    """)
    List<Robot> filtrarRobots(
            @Param("nombre") String nombre,
            @Param("categoria") CategoriaCompetencia categoria,
            @Param("idClub") String idClub
    );

    @Query(
            value = """
            SELECT r FROM Robot r
            LEFT JOIN FETCH r.competidor c
            LEFT JOIN FETCH c.usuario u
            LEFT JOIN FETCH c.clubActual cl
            WHERE (:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:categoria IS NULL OR r.categoria = :categoria)
              AND (:idClub IS NULL OR cl.idClub = :idClub)
            """,
            countQuery = """
            SELECT COUNT(r) FROM Robot r
            LEFT JOIN r.competidor c
            LEFT JOIN c.clubActual cl
            WHERE (:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:categoria IS NULL OR r.categoria = :categoria)
              AND (:idClub IS NULL OR cl.idClub = :idClub)
            """
    )
    Page<Robot> filtrarRobotsPage(
            @Param("nombre") String nombre,
            @Param("categoria") CategoriaCompetencia categoria,
            @Param("idClub") String idClub,
            Pageable pageable
    );

    @Query("""
    SELECT r
    FROM Robot r
    WHERE r.competidor.clubActual.idClub = :idClub
      AND r.categoria = :categoria
      AND r.estado = 'ACTIVO'  
      AND r.idRobot NOT IN (
          SELECT i.robot.idRobot
          FROM InscripcionTorneo i
          WHERE i.categoriaTorneo.torneo.idTorneo = :idTorneo
      )
    """)
    List<Robot> findRobotsDisponibles(
            @Param("idClub") String idClub,
            @Param("categoria") CategoriaCompetencia categoria,
            @Param("idTorneo") String idTorneo
    );

    @Query("""
    SELECT r
    FROM Robot r
    WHERE r.competidor.idCompetidor = :idCompetidor
      AND r.categoria = :categoria
      AND r.estado = 'ACTIVO'
      AND r.idRobot NOT IN (
          SELECT i.robot.idRobot
          FROM InscripcionTorneo i
          WHERE i.categoriaTorneo.torneo.idTorneo = :idTorneo
      )
    """)
    List<Robot> findRobotsDisponiblesCompetidor(
            @Param("idCompetidor") String idCompetidor,
            @Param("categoria") CategoriaCompetencia categoria,
            @Param("idTorneo") String idTorneo
    );

    List<Robot> findByCompetidorClubActualIdClubAndEstado(String idClub, EstadoRobot estado);

    @Query("SELECT COUNT(r) FROM Robot r WHERE r.competidor.clubActual.idClub = :idClub")
    long contarRobotsPorClub(@Param("idClub") String idClub);
}

