package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EstadoValidacion;
import com.robotech.robotech_backend.model.Juez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JuezRepository extends JpaRepository<Juez, String> {
    boolean existsByUsuario_IdUsuario(String idUsuario);

    Optional<Juez> findByUsuario_IdUsuario(String idUsuario);

    @Query("""
    SELECT j FROM Juez j
    WHERE j.idJuez = :idJuez
    AND j.estadoValidacion = :estado
""")
    Optional<Juez> buscarJuezAprobado(
            @Param("idJuez") String idJuez,
            @Param("estado") EstadoValidacion estado
    );
}
