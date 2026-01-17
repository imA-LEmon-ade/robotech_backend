package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "encuentros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encuentro {

    @Id
    @Column(length = 8, nullable = false, updatable = false)
    private String idEncuentro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria_torneo", referencedColumnName = "id_categoria_torneo", nullable = false)
    private CategoriaTorneo categoriaTorneo;

    // ✅ RELACIÓN DE CASCADA HACIA PARTICIPANTES
    @OneToMany(mappedBy = "encuentro", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EncuentroParticipante> participantes = new ArrayList<>();

    // ✅ RELACIÓN DE CASCADA HACIA CALIFICACIONES
    @OneToMany(mappedBy = "encuentro", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistorialCalificacion> calificaciones = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_juez", nullable = false)
    private Juez juez;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_coliseo", nullable = false)
    private Coliseo coliseo;

    @Column(nullable = false)
    private Integer ronda;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEncuentro tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEncuentro estado;

    private String ganadorIdReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoParticipante ganadorTipo;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoEncuentro.PROGRAMADO;
        if (ronda == null) ronda = 1;
        if (fecha == null) fecha = new Date();
    }
}