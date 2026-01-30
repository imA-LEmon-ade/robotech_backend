package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoEncuentro;
import com.robotech.robotech_backend.model.TipoEncuentro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncuentroPublicoDTO {
    private String idEncuentro;
    private String torneo;
    private String categoria;
    private TipoEncuentro tipo;
    private EstadoEncuentro estado;
    private Integer ronda;
    private Date fecha;
    private String coliseo;
    private List<ParticipanteEncuentroDTO> participantes;
}
