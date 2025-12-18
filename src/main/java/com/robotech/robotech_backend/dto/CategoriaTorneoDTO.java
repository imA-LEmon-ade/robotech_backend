package com.robotech.robotech_backend.dto;

import lombok.Data;

@Data
public class CategoriaTorneoDTO {

    private String categoria;        // Nombre (Minisumo, Megasumo, etc.)
    private Integer maxParticipantes; // Cupo máximo de robots
    private String descripcion;       // Opcional: descripción de la categoría
}
