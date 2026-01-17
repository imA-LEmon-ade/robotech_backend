package com.robotech.robotech_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.util.ArrayList; // ✅ Aseguramos la inicialización
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "torneos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Torneo {

    @Id
    @Column(name = "id_torneo", length = 8, nullable = false)
    private String idTorneo;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Temporal(TemporalType.DATE)
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    @Temporal(TemporalType.DATE)
    private Date fechaAperturaInscripcion;

    @Temporal(TemporalType.DATE)
    private Date fechaCierreInscripcion;

    @Column(nullable = false)
    private String estado;

    @Column
    private String creadoPor;

    @OneToMany(
            mappedBy = "torneo",
            cascade = CascadeType.ALL,
            orphanRemoval = true // ✅ Esto es la clave para el borrado
    )
    @JsonManagedReference
    @Builder.Default // ✅ Importante para que Lombok respete la inicialización
    private List<CategoriaTorneo> categorias = new ArrayList<>(); // ✅ Inicializada para evitar Nulls

    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @PrePersist
    public void prePersist() {
        if (idTorneo == null) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
            }
            idTorneo = sb.toString();
        }
    }
}