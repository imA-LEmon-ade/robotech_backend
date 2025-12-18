package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreoAndContrasenaHash(String correo, String contrasenaHash);
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);

    List<Usuario> findByEstado(String estado);
}

