package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class EditarClubDTO {

    private String nombre;
    private String correoContacto;
    private String telefonoContacto;
    private String direccionFiscal;
    private String estado;
}