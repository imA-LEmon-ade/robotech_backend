package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.enums.EstadoSubAdmin;
import com.robotech.robotech_backend.model.entity.SubAdministrador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface SubAdministradorRepository
        extends JpaRepository<SubAdministrador, String> {

    boolean existsByUsuario_IdUsuario(String idUsuario);

    Optional<SubAdministrador> findByUsuario_IdUsuario(String idUsuario);

    List<SubAdministrador> findByEstado(EstadoSubAdmin estado);

    @Query(
        value = """
        SELECT s FROM SubAdministrador s
        JOIN FETCH s.usuario u
        WHERE (:q IS NULL OR
            LOWER(COALESCE(u.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        """,
        countQuery = """
        SELECT COUNT(s) FROM SubAdministrador s
        JOIN s.usuario u
        WHERE (:q IS NULL OR
            LOWER(COALESCE(u.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        """
    )
    Page<SubAdministrador> buscar(@Param("q") String q, Pageable pageable);

    @Query(
        value = """
        SELECT s FROM SubAdministrador s
        JOIN FETCH s.usuario u
        WHERE
            (:nombre IS NULL OR :nombre = '' OR
                LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:dni IS NULL OR :dni = '' OR
                LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :dni, '%')))
            AND (:estado IS NULL OR :estado = '' OR
                UPPER(COALESCE(CONCAT(s.estado, ''), '')) = UPPER(:estado))
        """,
        countQuery = """
        SELECT COUNT(s) FROM SubAdministrador s
        JOIN s.usuario u
        WHERE
            (:nombre IS NULL OR :nombre = '' OR
                LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:dni IS NULL OR :dni = '' OR
                LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :dni, '%')))
            AND (:estado IS NULL OR :estado = '' OR
                UPPER(COALESCE(CONCAT(s.estado, ''), '')) = UPPER(:estado))
        """
    )
    Page<SubAdministrador> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("dni") String dni,
            @Param("estado") String estado,
            Pageable pageable
    );
}



