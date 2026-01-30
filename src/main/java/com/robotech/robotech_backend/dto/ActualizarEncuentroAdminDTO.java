package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoEncuentro;
import lombok.Data;

import java.util.Date;

@Data
public class ActualizarEncuentroAdminDTO {
    private String idJuez;
    private String idColiseo;
    private Integer ronda;
    private Date fecha;
    private EstadoEncuentro estado;
}
