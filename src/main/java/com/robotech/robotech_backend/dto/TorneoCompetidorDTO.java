package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorneoCompetidorDTO {
    private String idTorneo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String descripcion;

    private List<String> categorias;
    private List<String> modalidades;
    private List<String> robots;
    private List<String> estadosInscripcion;
    private Integer inscripciones;

    private String ganador;
    private List<ResultadoTorneoDTO> resultados;
}


