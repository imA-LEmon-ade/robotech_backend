package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.enums.EstadoTransferenciaPropietario;
import com.robotech.robotech_backend.model.entity.TransferenciaPropietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaPropietarioRepository extends JpaRepository<TransferenciaPropietario, String> {

    List<TransferenciaPropietario> findByClub_IdClubOrderByCreadoEnDesc(String idClub);

    List<TransferenciaPropietario> findByEstado(EstadoTransferenciaPropietario estado);

    boolean existsByClub_IdClubAndEstado(String idClub, EstadoTransferenciaPropietario estado);
}


