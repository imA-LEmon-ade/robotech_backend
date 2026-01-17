package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "historial_calificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCalificacion {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "encuentro_id_encuentro", referencedColumnName = "idEncuentro")
    private Encuentro encuentro;

    @Enumerated(EnumType.STRING)
    private TipoParticipante tipo;

    private String idReferencia; // robot o equipo

    private Integer puntaje; // 0 - 100

    private Date fecha;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().substring(0,8).toUpperCase();
        }
        if (fecha == null) {
            fecha = new Date();
        }
    }
}
