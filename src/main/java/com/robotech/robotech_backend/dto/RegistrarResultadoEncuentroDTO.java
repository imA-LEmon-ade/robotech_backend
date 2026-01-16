package com.robotech.robotech_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegistrarResultadoEncuentroDTO {
    private String idEncuentro;
    private List<CalificacionParticipanteDTO> calificaciones;
}