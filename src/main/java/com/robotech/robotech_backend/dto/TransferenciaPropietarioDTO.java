package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.EstadoTransferenciaPropietario;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TransferenciaPropietarioDTO {
    private String idTransferencia;
    private String idClub;
    private String nombreClub;

    private String idPropietarioActual;
    private String nombrePropietarioActual;
    private String correoPropietarioActual;

    private String idCompetidorNuevo;
    private String nombreCompetidorNuevo;
    private String correoCompetidorNuevo;

    private EstadoTransferenciaPropietario estado;
    private Date creadoEn;
    private Date actualizadoEn;
    private Date aprobadoEn;
}
