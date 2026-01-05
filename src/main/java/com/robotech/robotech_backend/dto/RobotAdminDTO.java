package com.robotech.robotech_backend.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobotAdminDTO {

    private String idRobot;
    private String nombre;
    private String nickname;
    private String categoria;
    private String club;
    private String competidor;
}

