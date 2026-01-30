package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EstadoTransferencia;
import com.robotech.robotech_backend.model.TransferenciaCompetidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransferenciaCompetidorRepository extends JpaRepository<TransferenciaCompetidor, String> {

    List<TransferenciaCompetidor> findByEstado(EstadoTransferencia estado);

    List<TransferenciaCompetidor> findByClubOrigen_IdClub(String idClub);

    List<TransferenciaCompetidor> findByClubDestino_IdClub(String idClub);

    boolean existsByCompetidor_IdCompetidorAndEstadoIn(String idCompetidor, Collection<EstadoTransferencia> estados);
}
