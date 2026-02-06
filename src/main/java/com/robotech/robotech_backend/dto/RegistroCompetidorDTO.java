package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class RegistroCompetidorDTO {
    private String dni;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String contrasena;
    private String codigoClub;
}


