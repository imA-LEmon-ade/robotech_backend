package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import com.robotech.robotech_backend.model.enums.ModalidadCategoria;
import lombok.Data;

@Data
public class CategoriaTorneoDTO {

    private CategoriaCompetencia categoria;
    private ModalidadCategoria modalidad;

    // Individual
    private Integer maxParticipantes;

    // Equipo
    private Integer maxEquipos;
    private Integer maxIntegrantesEquipo;

    private String descripcion;
}



