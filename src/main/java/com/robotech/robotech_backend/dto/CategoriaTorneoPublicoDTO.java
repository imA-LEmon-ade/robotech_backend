package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.ModalidadCategoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaTorneoPublicoDTO {
    private String idCategoriaTorneo;
    private String categoria;
    private ModalidadCategoria modalidad;
    private String descripcion;
    private Integer maxParticipantes;
    private Integer maxEquipos;
    private Integer maxIntegrantesEquipo;
    private Boolean inscripcionesCerradas;
}


