package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CrearClubDTO {

    // datos del club
    private String nombre;
    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;

    // datos del propietario (usuario)
    private String nombresPropietario;
    private String apellidosPropietario;
    private String correoPropietario;
    private String contrasenaPropietario;
    private String telefonoPropietario;
}
