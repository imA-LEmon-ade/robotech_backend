package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EstadoValidacion;
import com.robotech.robotech_backend.model.Juez;
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
}