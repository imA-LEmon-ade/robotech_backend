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

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column
    private String nombres;

    @Column
    private String apellidos;

    // ✅ CORRECCIÓN: Cambiado a nullable = true.
    // Si es obligatorio, el DTO debe validarlo, pero en BD es mejor permitir null si no hay dato.
    @Column(nullable = true, unique = true, length = 15)
    private String telefono;

    @Column(nullable = false)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

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

    @PrePersist
    public void prePersist() {
        if (this.idUsuario == null) {
            this.idUsuario = generarIdAlfanumerico();
        }

        if (this.estado == null) {
            this.estado = EstadoUsuario.PENDIENTE;
        }
        // Limpieza de campos para evitar errores de unicidad con strings vacíos
        if (this.telefono != null && this.telefono.isBlank()) {
            this.telefono = null;
        }
    }
}