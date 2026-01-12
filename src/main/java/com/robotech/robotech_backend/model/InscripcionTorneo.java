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
    private Date anuladaEn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInscripcion;

    @PrePersist
    public void prePersist() {
        if (idInscripcion == null) {
            idInscripcion = UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        }
        if (fechaInscripcion == null) {
            fechaInscripcion = new Date();
        }
        if (estado == null) {
            estado = EstadoInscripcion.ACTIVA;
        }
    }
}
