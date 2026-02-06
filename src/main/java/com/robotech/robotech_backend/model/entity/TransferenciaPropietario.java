package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "transferencias_propietario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaPropietario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String idTransferencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_propietario_actual", nullable = false)
    private Usuario propietarioActual;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_competidor_nuevo", nullable = false)
    private Competidor competidorNuevo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransferenciaPropietario estado;

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
            estado = EstadoTransferenciaPropietario.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = new Date();
    }
}



