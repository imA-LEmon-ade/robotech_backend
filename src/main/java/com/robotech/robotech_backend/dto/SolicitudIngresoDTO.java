package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoSolicitudIngresoClub;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class SolicitudIngresoDTO {
    private String idSolicitud;
    private String idClub;
    private String nombreClub;
    private String idCompetidor;
    private String nombreCompetidor;
    private String correoCompetidor;
    private EstadoSolicitudIngresoClub estado;
    private Date creadoEn;
    private Date actualizadoEn;
    private Date aprobadoEn;
}
