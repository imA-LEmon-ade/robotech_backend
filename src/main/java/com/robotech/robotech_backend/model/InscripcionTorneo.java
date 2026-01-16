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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String idInscripcion;

    @ManyToOne(optional = false)
    private CategoriaTorneo categoriaTorneo;

    @ManyToOne(optional = false)
    private Robot robot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado;

    @Column(length = 500)
    private String motivoAnulacion;

    private String anuladaPor;

    @Temporal(TemporalType.TIMESTAMP)
    private Date anuladaEn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaInscripcion;

    @PrePersist
    public void prePersist() {
        if (fechaInscripcion == null) {
            fechaInscripcion = new Date();
        }
        if (estado == null) {
            estado = EstadoInscripcion.ACTIVA;
        }
    }
}
