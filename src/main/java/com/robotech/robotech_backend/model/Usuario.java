package com.robotech.robotech_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;


@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @Column(length = 8)
    private String idUsuario;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column
    private String nombres;

    @Column
    private String apellidos;


    @Column(nullable = false, unique = true, length = 9)
    private String telefono;

    @Column(nullable = false)
    private String contrasenaHash;  // Luego usaremos BCrypt

    @Column(nullable = false)
    private String rol;
    // ADMINISTRADOR, SUBADMINISTRADOR, JUEZ, CLUB, COMPETIDOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado;


    private static final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generarIdAlfanumerico() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHA_NUM.charAt(RANDOM.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    // ---------- SE EJECUTA ANTES DE INSERTAR EN LA BD ----------
    @PrePersist
    public void prePersist() {
        if (this.idUsuario == null) {
            this.idUsuario = generarIdAlfanumerico();
        }
    }
}
