package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Encuentro;
import com.robotech.robotech_backend.model.EstadoEncuentro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncuentroRepository extends JpaRepository<Encuentro, String> {

    List<Encuentro> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    List<Encuentro> findByJuezIdJuez(String idJuez);

    // ✅ NUEVO: Cuenta cuántos encuentros de UN TORNEO no han terminado aún
    // Esta consulta viaja: Encuentro -> CategoriaTorneo -> Torneo
    long countByCategoriaTorneo_Torneo_IdTorneoAndEstadoNot(String idTorneo, EstadoEncuentro estado);
}