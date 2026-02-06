package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.TipoParticipante;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipanteEncuentroDTO {
    private String idReferencia;
    private String nombre;
    private TipoParticipante tipo;
    private Integer calificacion;
    private Boolean ganador;
}

