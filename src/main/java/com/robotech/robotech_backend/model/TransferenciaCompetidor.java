package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "transferencias_competidor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaCompetidor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String idTransferencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_competidor", nullable = false)
    private Competidor competidor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_club_origen", nullable = false)
    private Club clubOrigen;

    @ManyToOne(optional = true)
    @JoinColumn(name = "id_club_destino")
    private Club clubDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransferencia estado;

    @Column(nullable = true)
    private Integer precio;

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
            estado = EstadoTransferencia.EN_VENTA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = new Date();
    }
}
