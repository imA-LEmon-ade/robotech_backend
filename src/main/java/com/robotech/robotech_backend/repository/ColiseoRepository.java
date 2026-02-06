package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.Coliseo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColiseoRepository extends JpaRepository<Coliseo, String> {
    @Query("""
        SELECT c FROM Coliseo c
        WHERE (:q IS NULL OR
            LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(c.ubicacion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    Page<Coliseo> buscar(@Param("q") String q, Pageable pageable);
}


