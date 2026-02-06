package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoTorneoDTO {
    private String nombre; // Aquí llegará "Paquito"
    private Long puntaje;  // Aquí llegará 84
}

