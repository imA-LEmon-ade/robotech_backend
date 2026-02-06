package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "subadministradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubAdministrador {

    @Id
    @Column(name = "id_usuario", length = 8)
    private String idUsuario;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSubAdmin estado;

    @Column(name = "creado_por")
    private String creadoPor;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creado_en")
    private Date creadoEn;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoSubAdmin.ACTIVO;
        }
        if (this.creadoEn == null) {
            this.creadoEn = new Date();
        }
    }
}



