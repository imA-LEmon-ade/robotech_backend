package com.robotech.robotech_backend.dto;

import lombok.Data;
import java.time.LocalDateTime; // Usaremos LocalDateTime para ser precisos

@Data
public class CrearTorneoDTO {
    private String nombre;
    private String descripcion;

    // Cambiamos a LocalDateTime para coincidir con el input datetime-local del front
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaAperturaInscripcion;
    private LocalDateTime fechaCierreInscripcion;

    // ⚠️ IMPORTANTE: Agregamos este campo para recibir el estado
    private String estado;
}