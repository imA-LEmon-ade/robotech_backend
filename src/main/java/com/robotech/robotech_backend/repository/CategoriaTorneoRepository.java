package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.entity.CategoriaTorneo;
import com.robotech.robotech_backend.model.entity.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaTorneoRepository extends JpaRepository<CategoriaTorneo, String> {
    List<CategoriaTorneo> findByTorneoIdTorneo(String idTorneo);

    List<CategoriaTorneo> findByTorneo(Torneo torneo);
}


