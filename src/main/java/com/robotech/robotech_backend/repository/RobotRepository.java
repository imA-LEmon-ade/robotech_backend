package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.CategoriaCompetencia;
import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.EstadoRobot;
import com.robotech.robotech_backend.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RobotRepository extends JpaRepository<Robot, String> {

    // Obtener todos los robots de un competidor
    List<Robot> findByCompetidor_IdCompetidor(String idCompetidor);

    // Verificar si ya registró un robot en la categoría
    boolean existsByCompetidor_IdCompetidorAndCategoria(
            String idCompetidor,
            CategoriaCompetencia categoria
    );

    // Validaciones de unicidad
    boolean existsByNickname(String nickname);
    boolean existsByNombre(String nombre);

    // Listar por Club (Entidad completa)
    List<Robot> findByCompetidor_ClubActual(Club club);

    int countByCompetidor_IdCompetidor(String idCompetidor);

    // -------------------------------------------------------
    // 🔍 FILTRO AVANZADO (Para el Admin)
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // 🏆 ROBOTS DISPONIBLES PARA INSCRIPCIÓN (Para el Club)
    // -------------------------------------------------------
    // Nota: Asegúrate de que tu entidad de inscripción se llame 'InscripcionTorneo'.
    // Si se llama solo 'Inscripcion', cambia la palabra en el FROM de la subquery.
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

    List<Robot> findByCompetidorClubActualIdClubAndEstado(
            String idClub,
            EstadoRobot estado
    );
    // En RobotRepository.java

    @Query("SELECT COUNT(r) FROM Robot r WHERE r.competidor.clubActual.idClub = :idClub")
    long contarRobotsPorClub(@Param("idClub") String idClub);
}