package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankingDTO {
    private String idReferencia;
    private String nombre;
    private Integer victorias;
    private Integer empates;
    private Integer derrotas;
    private Double promedioPuntaje;
    private Integer puntosRanking;
}
