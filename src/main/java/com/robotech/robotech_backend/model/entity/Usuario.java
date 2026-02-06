package com.robotech.robotech_backend.model.entity;


import com.robotech.robotech_backend.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

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

    // ? CORRECCI?N: Cambiado a nullable = true.
    // Si es obligatorio, el DTO debe validarlo, pero en BD es mejor permitir null si no hay dato.
    @Column(nullable = true, unique = true, length = 15)
    private String telefono;

    @Column(nullable = false)
    private String contrasenaHash;

    @Column(unique = true)
    private String passwordResetToken;

    @Column
    private java.time.LocalDateTime passwordResetTokenExpiryDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "id_usuario"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    @Builder.Default
    private Set<RolUsuario> roles = new HashSet<>();

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

    public boolean tieneRol(RolUsuario rol) {
        return roles != null && roles.contains(rol);
    }

    @PrePersist
    public void prePersist() {
        if (this.idUsuario == null) {
            this.idUsuario = generarIdAlfanumerico();
        }

        if (this.estado == null) {
            this.estado = EstadoUsuario.PENDIENTE;
        }
        // Limpieza de campos para evitar errores de unicidad con strings vac?os
        if (this.telefono != null && this.telefono.isBlank()) {
            this.telefono = null;
        }
    }
}



