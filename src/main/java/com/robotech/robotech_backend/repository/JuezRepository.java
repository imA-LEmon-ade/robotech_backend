package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.model.entity.Juez;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JuezRepository extends JpaRepository<Juez, String> {

    // ✅ Consulta optimizada para traer al Juez con su Usuario (nombres/apellidos) de un solo golpe
    @Query("SELECT j FROM Juez j JOIN FETCH j.usuario")
    List<Juez> findAllWithUsuario();

    boolean existsByUsuario_IdUsuario(String idUsuario);

    Optional<Juez> findByUsuario_IdUsuario(String idUsuario);
    boolean existsByLicencia(String licencia);

    Optional<Juez> findByLicencia(String licencia);


    // ✅ Lista jueces por estado, cargando también sus datos de usuario
    @Query("SELECT j FROM Juez j LEFT JOIN FETCH j.usuario WHERE j.estadoValidacion = :estado")
    List<Juez> findByEstadoValidacion(@Param("estado") EstadoValidacion estado);

    // 🔎 BUSCAR JUEZ PUNTUAL
    @Query("""
        SELECT j FROM Juez j
        LEFT JOIN FETCH j.usuario
        WHERE j.idJuez = :idJuez
        AND j.estadoValidacion = :estado
    """)
    Optional<Juez> buscarJuezAprobado(
            @Param("idJuez") String idJuez,
            @Param("estado") EstadoValidacion estado
    );

    @Query(
        value = """
        SELECT j FROM Juez j
        JOIN FETCH j.usuario u
        WHERE (:q IS NULL OR
            LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(j.licencia, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        """,
        countQuery = """
        SELECT COUNT(j) FROM Juez j
        JOIN j.usuario u
        WHERE (:q IS NULL OR
            LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(j.licencia, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        """
    )
    Page<Juez> buscar(@Param("q") String q, Pageable pageable);

    @Query(
            value = """
        SELECT j FROM Juez j
        JOIN FETCH j.usuario u
        WHERE
            (:nombre IS NULL OR :nombre = '' OR
                LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:dni IS NULL OR :dni = '' OR
                LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :dni, '%')))
            AND (:licencia IS NULL OR :licencia = '' OR
                LOWER(COALESCE(j.licencia, '')) LIKE LOWER(CONCAT('%', :licencia, '%')))
        """,
            countQuery = """
        SELECT COUNT(j) FROM Juez j
        JOIN j.usuario u
        WHERE
            (:nombre IS NULL OR :nombre = '' OR
                LOWER(CONCAT(COALESCE(u.nombres, ''), ' ', COALESCE(u.apellidos, ''))) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:dni IS NULL OR :dni = '' OR
                LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :dni, '%')))
            AND (:licencia IS NULL OR :licencia = '' OR
                LOWER(COALESCE(j.licencia, '')) LIKE LOWER(CONCAT('%', :licencia, '%')))
        """
    )
    Page<Juez> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("dni") String dni,
            @Param("licencia") String licencia,
            Pageable pageable
    );
}

