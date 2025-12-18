package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "equipos_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoTorneo {

    @Id
    @Column(length = 8)
    private String idEquipo;

    @ManyToOne(optional = false)
    private Club club;

    @ManyToOne(optional = false)
    private CategoriaTorneo categoriaTorneo;

    @OneToMany
    @JoinTable(
            name = "equipo_robots",
            joinColumns = @JoinColumn(name = "id_equipo"),
            inverseJoinColumns = @JoinColumn(name = "id_robot")
    )
    private List<Robot> robots;

    @Column(nullable = false)
    private String estado;
    // PENDIENTE, APROBADO, RECHAZADO

    @Column(nullable = false)
    private Date fechaInscripcion;

    @PrePersist
    public void prePersist() {
        if (idEquipo == null) {
            idEquipo = UUID.randomUUID()
                    .toString()
                    .substring(0,8)
                    .toUpperCase();
        }
        fechaInscripcion = new Date();
        estado = "PENDIENTE";
    }
}
