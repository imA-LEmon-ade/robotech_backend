package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompetidorResponseDTO {
    private String idUsuario;
    private String nombres;
    private String apellidos;
    private String dni;
    private String correo;
    private String clubNombre;
    private String estadoValidacion;
    String telefono;
}