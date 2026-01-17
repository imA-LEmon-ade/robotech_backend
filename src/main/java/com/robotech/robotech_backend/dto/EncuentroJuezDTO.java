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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncuentroJuezDTO {

    private String idEncuentro;
    private String nombreTorneo; // ✅ AGREGADO: Para que coincida con el Service
    private String categoria;
    private TipoEncuentro tipo;
    private EstadoEncuentro estado;
    private String coliseo;
    private Date fecha;

    private List<ParticipanteEncuentroDTO> participantes;
}