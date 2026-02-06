package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
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
    @Column(name = "id_juez", length = 8)
    private String idJuez;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String licencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoValidacion estadoValidacion;

    private String creadoPor;
    private String validadoPor;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date validadoEn;

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
        if (this.idJuez == null) {
            this.idJuez = generarId();
        }
        if (this.creadoEn == null) {
            this.creadoEn = new Date();
        }
        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacion.PENDIENTE;
        }
    }
}



