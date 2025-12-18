package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class JuezDTO {
    private String correo;
    private String telefono;
    private String contrasena;
    private String licencia;

    private  String creadoPor;
}