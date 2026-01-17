package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;

@Entity
@Table(name = "robots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Robot {

    @Id
    @Column(name = "id_robot", length = 8, nullable = false, updatable = false)
    private String idRobot;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaCompetencia categoria;

    @Column(nullable = false, unique = true)
    private String nickname;

    @ManyToOne
    @JoinColumn(name = "id_competidor", referencedColumnName = "id_competidor", nullable = false) // ✅ Ahora sí coinciden
    private Competidor competidor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRobot estado;

    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarId() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        return sb.toString();
    }

    @PrePersist
    public void prePersist() {
        if (idRobot == null) idRobot = generarId();
    }
}