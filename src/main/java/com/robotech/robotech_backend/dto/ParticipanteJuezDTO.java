package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.TipoParticipante;

public record ParticipanteJuezDTO(
        String idReferencia,
        TipoParticipante tipo,
        String nombre
) {}

