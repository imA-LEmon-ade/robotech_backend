package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "encuentro_participantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncuentroParticipante {

    @Id
    private String id;

    @ManyToOne
    private Encuentro encuentro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoParticipante tipo;

    private String idReferencia; // idRobot o idEquipo

    private Integer calificacion;
    private Boolean ganador;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
