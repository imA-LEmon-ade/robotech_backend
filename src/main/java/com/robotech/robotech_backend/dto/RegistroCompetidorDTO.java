package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class RegistroCompetidorDTO {
    private String nombre;
    private String apellido;
    private String dni;
    private String correo;
    private String telefono;
    private String contrasena;
    private String codigoClub;
}
