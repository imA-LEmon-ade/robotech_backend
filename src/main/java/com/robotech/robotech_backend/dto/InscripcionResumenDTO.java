package com.robotech.robotech_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; // Recomendado para facilitar la construcción
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder // Nos permite crear objetos sin liarnos con el orden del constructor
@AllArgsConstructor
@NoArgsConstructor // Necesario para JSON y JPA a veces
public class InscripcionResumenDTO {

    private String idInscripcion;
    private String torneo;
    private String categoria;
    private String modalidad; // INDIVIDUAL / EQUIPO
    private List<String> robots; // Mantenemos tu lista
    private String estado;

    // === CAMPOS NUEVOS (Agregados para la UI) ===
    private LocalDate fechaRegistro;
    private String torneoFecha;
}

