package com.robotech.robotech_backend.dto;

public record RobotResponseDTO(
        String idRobot,
        String nombre,
        String nickname,
        String categoria,
        String estado
) {}