package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;
import java.util.Date;

@Entity
@Table(name = "jueces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Juez {

    @Id
    @Column(length = 8, nullable = false, unique = true)
    private String idJuez;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String licencia;

    //flujo correctamente soportado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoValidacion estadoValidacion;
    // PENDIENTE, APROBADO, RECHAZADO
    private String creadoPor;          // id del admin que creó el juez
    private String validadoPor;        // id del admin que valida el juez
    private Date creadoEn;             // fecha creación
    private Date validadoEn;           // fecha validación (solo cuando se aprueba o rechaza)

    // ------------------------------------------------------------
    // GENERADOR DE ID ALFANUMÉRICO (8 CARACTERES)
    // ------------------------------------------------------------
    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarId() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------
    // PRE-PERSIST
    // ------------------------------------------------------------
    @PrePersist
    public void prePersist() {
        if (this.idJuez == null) {
            this.idJuez = generarId();
        }

        if (this.creadoEn == null) {
            this.creadoEn = new Date();
        }

        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacion.PENDIENTE;
        }

        // validadoEn NO se coloca aquí
        // solo se llenará cuando el admin apruebe o rechace
    }
}
