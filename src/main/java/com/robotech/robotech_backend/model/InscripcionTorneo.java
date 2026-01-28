package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "inscripciones_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_inscripcion", length = 36)
    private String idInscripcion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_torneo_id_categoria_torneo", nullable = false) // ✅ Añadido nullable=false
    private CategoriaTorneo categoriaTorneo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_robot", referencedColumnName = "id_robot", nullable = false) // ✅ Forzamos que use id_robot
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
            estado = EstadoInscripcion.ACTIVADA;
        }
    }
}