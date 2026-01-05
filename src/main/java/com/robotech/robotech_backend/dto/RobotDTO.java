package com.robotech.robotech_backend.dto;

import com.robotech.robotech_backend.model.CategoriaCompetencia;
import lombok.Data;

@Data
public class RobotDTO {
    private String nombre;
    private CategoriaCompetencia categoria;
    private String nickname;
}
