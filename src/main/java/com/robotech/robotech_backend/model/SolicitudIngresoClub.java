package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "solicitudes_ingreso_club")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudIngresoClub {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String idSolicitud;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_competidor", nullable = false)
    private Competidor competidor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitudIngresoClub estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date creadoEn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actualizadoEn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date aprobadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) {
            creadoEn = new Date();
        }
        if (estado == null) {
            estado = EstadoSolicitudIngresoClub.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = new Date();
    }
}
