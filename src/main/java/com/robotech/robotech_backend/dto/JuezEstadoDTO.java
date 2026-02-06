package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JuezEstadoDTO {
    private String idJuez;
    private String licencia;
    private EstadoValidacion estado;
}


