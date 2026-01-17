package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.util.*;

@Entity
@Table(name = "competidores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competidor {

    @Id
    @Column(name = "id_competidor", length = 8, nullable = false, unique = true) // ✅ Agregamos el name
    private String idCompetidor;

    // PERFIL BASE
    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    // IDENTIDAD COMPETITIVA
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoValidacion estadoValidacion;
    // PENDIENTE, APROBADO, RECHAZADO

    @Column(name = "foto_url")
    private String fotoUrl;

    // CLUB ACTUAL
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_club_actual", nullable = false)
    private Club clubActual;

    // ROBOTS
    @OneToMany(mappedBy = "competidor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Robot> robots = new ArrayList<>();

    // ============================
    // ID AUTO-GENERADO
    // ============================
    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarId() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    @PrePersist
    public void prePersist() {
        if (this.idCompetidor == null) {
            this.idCompetidor = generarId();
        }
        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacion.PENDIENTE;
        }
    }
}
