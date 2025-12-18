package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CrearClubDTO {

    private String nombre;
    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;

    // datos propietario
    private String correoPropietario;
    private String contrasenaPropietario;
    private String telefonoPropietario;
}
