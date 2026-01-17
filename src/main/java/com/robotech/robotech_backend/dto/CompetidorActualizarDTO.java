package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CompetidorActualizarDTO {
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String dni;
}
