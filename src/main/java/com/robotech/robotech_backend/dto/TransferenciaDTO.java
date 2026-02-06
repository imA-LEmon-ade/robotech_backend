package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.EstadoTransferencia;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TransferenciaDTO {
    private String idTransferencia;
    private String idCompetidor;
    private String nombreCompetidor;
    private String idClubOrigen;
    private String nombreClubOrigen;
    private String idClubDestino;
    private String nombreClubDestino;
    private EstadoTransferencia estado;
    private Integer precio;
    private Date creadoEn;
    private Date actualizadoEn;
    private Date aprobadoEn;
}


