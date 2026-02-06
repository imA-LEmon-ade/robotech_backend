package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoEncuentro;
import com.robotech.robotech_backend.model.enums.TipoEncuentro;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EncuentroAdminDTO {

    private String idEncuentro;

    private String torneo;
    private String categoria;

    private TipoEncuentro tipo;
    private Integer ronda;
    private EstadoEncuentro estado;

    private String juez;
    private String coliseo;
}


