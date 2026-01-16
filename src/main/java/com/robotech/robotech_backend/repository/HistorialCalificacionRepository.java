package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.HistorialCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCalificacionRepository
        extends JpaRepository<HistorialCalificacion, String> {

    List<HistorialCalificacion>
    findByEncuentroCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);
}
