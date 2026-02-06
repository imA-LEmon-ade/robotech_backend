package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.enums.CategoriaCompetencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotDTO {
    private String nombre;
    private String categoria; //
    private String nickname;
    private String idRobot;
}


