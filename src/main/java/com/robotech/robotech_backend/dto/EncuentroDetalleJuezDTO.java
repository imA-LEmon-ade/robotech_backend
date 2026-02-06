package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoEncuentro;
import com.robotech.robotech_backend.model.enums.TipoEncuentro;

import java.util.List;

public record EncuentroDetalleJuezDTO(
        String idEncuentro,
        EstadoEncuentro estado,
        TipoEncuentro tipo,
        Integer ronda,
        List<ParticipanteJuezDTO> participantes
) {}



