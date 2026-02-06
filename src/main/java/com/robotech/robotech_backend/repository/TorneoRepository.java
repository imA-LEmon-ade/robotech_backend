package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.Torneo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, String> {

    // Torneos por estado
    List<Torneo> findByEstado(String estado);

    // Torneos por múltiples estados
    List<Torneo> findByEstadoIn(List<String> estados);

    // Torneos creados por un usuario (admin / subadmin)
    List<Torneo> findByCreadoPor(String creadoPor);

    @Query("""
        SELECT t FROM Torneo t
        WHERE (:q IS NULL OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Torneo> buscar(@Param("q") String q, Pageable pageable);

}


