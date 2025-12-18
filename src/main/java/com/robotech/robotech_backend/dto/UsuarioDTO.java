package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class UsuarioDTO {

    private String correo;
    private String telefono;
    private String contrasena;
    private String rol;
    private String estado;
}
