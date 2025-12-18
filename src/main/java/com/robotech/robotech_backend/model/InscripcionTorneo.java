package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "inscripciones_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionTorneo {

    @Id
    @Column(length = 8)
    private String idInscripcion;

    @ManyToOne
    @JoinColumn(name = "id_categoria_torneo", nullable = false)
    private CategoriaTorneo categoriaTorneo;

    @ManyToOne
    @JoinColumn(name = "id_robot", nullable = false)
    private Robot robot;

    @Column(nullable = false)
    private String estado;  // PENDIENTE, APROBADO, RECHAZADO

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInscripcion;

    @PrePersist
    public void prePersist() {
        if (idInscripcion == null) {
            idInscripcion = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        fechaInscripcion = new Date();
        estado = "PENDIENTE";
    }
}
