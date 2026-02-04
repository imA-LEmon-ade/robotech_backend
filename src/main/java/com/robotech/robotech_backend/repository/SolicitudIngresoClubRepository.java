package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EstadoSolicitudIngresoClub;
import com.robotech.robotech_backend.model.SolicitudIngresoClub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudIngresoClubRepository extends JpaRepository<SolicitudIngresoClub, String> {
    List<SolicitudIngresoClub> findByCompetidor_IdCompetidorOrderByCreadoEnDesc(String idCompetidor);

    List<SolicitudIngresoClub> findByClub_IdClubAndEstado(String idClub, EstadoSolicitudIngresoClub estado);

    boolean existsByCompetidor_IdCompetidorAndEstado(String idCompetidor, EstadoSolicitudIngresoClub estado);
}
