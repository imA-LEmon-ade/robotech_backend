package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, String> {
    Optional<Club> findByUsuario_IdUsuario(String idUsuario);
    List<Club> findByNombreContainingIgnoreCase(String nombre);
    Optional<Club> findByUsuario(Usuario usuario);
    Optional<Club> findByCodigoClub(String codigoClub);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCorreoContacto(String correoContacto);


}
