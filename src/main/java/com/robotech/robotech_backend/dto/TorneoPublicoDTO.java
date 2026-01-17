package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor // ✅ Este genera el constructor con los 9 campos
@NoArgsConstructor
public class TorneoPublicoDTO {
    private String idTorneo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String descripcion;
    private List<String> categorias;

    // ✅ NUEVOS CAMPOS PARA RESULTADOS
    private String ganador;
    private List<ResultadoTorneoDTO> resultados;

    // ✅ Constructor de compatibilidad (Corregido para no perder datos)
    public TorneoPublicoDTO(String idTorneo, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado, String descripcion, List<String> categorias) {
        this.idTorneo = idTorneo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.descripcion = descripcion;
        this.categorias = categorias;
        this.ganador = null; // Inicializado por defecto
        this.resultados = new java.util.ArrayList<>(); // Inicializado vacío
    }
}