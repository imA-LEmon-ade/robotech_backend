package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.CodigoRegistroCompetidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodigoRegistroCompetidorRepository extends JpaRepository<CodigoRegistroCompetidor, String> {

    Optional<CodigoRegistroCompetidor> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
    List<CodigoRegistroCompetidor> findByClub_IdClub(String idClub);

    // ✅ Método para contar (Este es el que usaremos para el contador de la interfaz)
    long countByClubIdClub(String idClub);
}
