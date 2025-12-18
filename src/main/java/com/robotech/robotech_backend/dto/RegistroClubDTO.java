package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class RegistroClubDTO {
    private String nombre;
    private String correo;
    private String telefono;
    private String direccionFiscal;
    private String contrasena;
}

