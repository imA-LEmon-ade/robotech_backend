package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.*;

public interface CompetidorRepository extends JpaRepository<Competidor, String> {
    boolean existsByUsuario_IdUsuario(String idUsuario);
    List<Competidor> findByClubActual_IdClub(String idClub);
    Optional<Competidor> findByUsuario_IdUsuario(String idUsuario);

    @Query("""
        SELECT c
        FROM Competidor c
        JOIN FETCH c.usuario u
        LEFT JOIN FETCH c.clubActual ca
        WHERE u.idUsuario = :idUsuario
    """)
    Optional<Competidor> findByUsuarioIdUsuarioFetch(@Param("idUsuario") String idUsuario);
    boolean existsByUsuario_Dni(String dni);
    long countByClubActual_IdClub(String idClub);

    @Query(
        value = """
            SELECT c
            FROM Competidor c
            JOIN c.usuario u
            LEFT JOIN c.clubActual ca
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(ca.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
        countQuery = """
            SELECT COUNT(c)
            FROM Competidor c
            JOIN c.usuario u
            LEFT JOIN c.clubActual ca
            WHERE (:q IS NULL OR :q = '' OR
                   LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(ca.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
            """
    )
    Page<Competidor> buscarPublico(@Param("q") String q, Pageable pageable);

}


