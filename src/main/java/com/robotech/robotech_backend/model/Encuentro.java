package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    @JoinColumn(name = "id_categoria_torneo", nullable = false)
    private CategoriaTorneo categoriaTorneo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_juez", nullable = false)
    private Juez juez;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_coliseo", nullable = false)
    private Coliseo coliseo;

    @Column(nullable = false)
    private Integer ronda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEncuentro tipo; // 🔥 AQUÍ

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEncuentro estado;

    @PrePersist
    public void prePersist() {

        if (idEncuentro == null) {
            idEncuentro = UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        }

        if (estado == null) {
            estado = EstadoEncuentro.PROGRAMADO;
        }

        if (ronda == null) {
            ronda = 1;
        }
    }
}
