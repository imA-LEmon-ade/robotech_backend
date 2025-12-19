package com.robotech.robotech_backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class CrearTorneoDTO {
    private String nombre;
    private String descripcion;
    private Date fechaInicio;
    private Date fechaFin;
    private Date fechaAperturaInscripcion;
    private Date fechaCierreInscripcion;
    private String tipo; // INDIVIDUAL o EQUIPOS
    private Integer maxParticipantes;
    private Integer numeroEncuentros;
}
