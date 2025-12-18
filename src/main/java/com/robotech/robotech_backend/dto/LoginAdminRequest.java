package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class LoginAdminRequest {
    private String correo;
    private String contrasena;
}
