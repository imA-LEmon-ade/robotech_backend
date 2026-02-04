package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Encuentro;
import com.robotech.robotech_backend.model.EstadoEncuentro;
import com.robotech.robotech_backend.model.TipoEncuentro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncuentroRepository extends JpaRepository<Encuentro, String> {

    boolean existsByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    List<Encuentro> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    void deleteByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    List<Encuentro> findByJuezIdJuez(String idJuez);

    List<Encuentro> findByCategoriaTorneoIdCategoriaTorneoAndTipoAndRonda(String idCategoriaTorneo, TipoEncuentro tipo, Integer ronda);

    boolean existsByCategoriaTorneoIdCategoriaTorneoAndTipoAndRonda(String idCategoriaTorneo, TipoEncuentro tipo, Integer ronda);

    // ✅ NUEVO: Cuenta cuántos encuentros de UN TORNEO no han terminado aún
    // Esta consulta viaja: Encuentro -> CategoriaTorneo -> Torneo
    long countByCategoriaTorneo_Torneo_IdTorneoAndEstadoNot(String idTorneo, EstadoEncuentro estado);

    long countByJuezIdJuezAndEstadoNot(String idJuez, EstadoEncuentro estado);
}
