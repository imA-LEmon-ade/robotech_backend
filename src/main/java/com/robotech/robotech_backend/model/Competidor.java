package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competidores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competidor {

    /**
     * 🔑 MISMO ID QUE USUARIO
     * No se genera aquí, viene del Usuario
     */
    @Id
    @Column(name = "id_usuario", length = 8)
    private String idCompetidor;

    /**
     * PERFIL BASE
     * @MapsId => usa el id_usuario como PK y FK
     */
    @MapsId
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    /**
     * VALIDACIÓN DEL PERFIL COMPETIDOR
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoValidacion estadoValidacion;

    /**
     * FOTO DE PERFIL
     */
    @Column(name = "foto_url")
    private String fotoUrl;

    /**
     * CLUB ACTUAL
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_club_actual", nullable = true)
    private Club clubActual;

    /**
     * ROBOTS DEL COMPETIDOR
     */
    @OneToMany(mappedBy = "competidor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Robot> robots = new ArrayList<>();

    // ------------------------------------------------------------
    //   VALORES POR DEFECTO
    // ------------------------------------------------------------
    @PrePersist
    public void prePersist() {
        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacion.PENDIENTE;
        }
    }
}
