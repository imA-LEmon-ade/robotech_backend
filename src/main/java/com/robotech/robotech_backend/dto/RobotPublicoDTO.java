package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotPublicoDTO {
    // El orden de estas variables define el orden del constructor
    private String idRobot;
    private String nombre;
    private String categoria;
    private String nickname;
    private String nombreDueño;
    private String nombreClub;
}