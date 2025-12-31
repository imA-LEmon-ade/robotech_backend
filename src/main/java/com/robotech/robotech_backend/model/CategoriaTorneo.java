package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categorias_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idCategoriaTorneo;

    @ManyToOne
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;

    @Column(nullable = false)
    private String categoria; // Minisumo, Sumo, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadCategoria modalidad; // INDIVIDUAL | EQUIPO

    // Para INDIVIDUAL
    private Integer maxParticipantes;

    // Para EQUIPO
    private Integer maxEquipos;
    private Integer maxIntegrantesEquipo;

    private String descripcion;
}