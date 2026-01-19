package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CrearSubAdminDTO {

    private String dni;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String contrasena;
}
