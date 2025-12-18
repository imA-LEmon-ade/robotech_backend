package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;
import java.util.Date;

@Entity
@Table(name = "subadministradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubAdministrador {

    @Id
    @Column(length = 8, nullable = false, unique = true)
    private String idSubadmin;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    private String descripcionCargo; // opcional: funciones del subadmin

    private String estado; // ACTIVO, INACTIVO, SUSPENDIDO
    private String creadoPor;
    private Date creadoEn;

    // ------------------------------------------------------------
    //      GENERADOR DE ID ALFANUMÉRICO (8 CARACTERES)
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
    //     SE EJECUTA AUTOMÁTICAMENTE ANTES DE INSERTAR EN BD
    // ------------------------------------------------------------
    @PrePersist
    public void prePersist() {
        if (this.idSubadmin == null) {
            this.idSubadmin = generarId();
        }
        if (this.creadoEn == null) {
            this.creadoEn = new Date();
        }
    }
}
