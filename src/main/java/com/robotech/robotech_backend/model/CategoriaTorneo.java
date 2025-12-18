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
    @Column(length = 8)
    private String idCategoriaTorneo;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;


    @Column(nullable = true)
    private String categoria; // Ej: MINISUMO, MEGASUMO, SOCCER...

    @Column(nullable = false)
    private Integer maxParticipantes;

    @Column(nullable = false)
    private Integer maxIntegrantesEquipo;


    @Column(nullable = false)
    private String descripcion; // ← ESTE FALTABA

    @OneToMany(mappedBy = "categoriaTorneo")
    private List<InscripcionTorneo> inscripciones;

    @PrePersist
    public void prePersist() {
        if (idCategoriaTorneo == null) {
            idCategoriaTorneo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
