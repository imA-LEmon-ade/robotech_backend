package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;
import java.util.Date;

@Entity
@Table(name = "codigos_registro_competidor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoRegistroCompetidor {

    @Id
    @Column(length = 8)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    private Date creadoEn;
    private Date expiraEn;

    private boolean usado;

    @Column(nullable = false)
    private int limiteUso; // siempre será 1 para tu caso

    @Column(nullable = false)
    private int usosActuales = 0;


    // ---- Generador de código (8 caracteres alfanuméricos) ----
    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generarCodigo() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    @PrePersist
    public void prePersist() {
        if (codigo == null) codigo = generarCodigo();
        if (creadoEn == null) creadoEn = new Date();

        // 👇 YA NO TOCAMOS expiraEn AQUÍ
        // Se asume que el servicio lo setea antes de guardar.

        usado = false;

        if (limiteUso <= 0) {
            limiteUso = 1; // por si acaso
        }

        if (usosActuales < 0) {
            usosActuales = 0;
        }
    }

}
