package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Club;
import com.robotech.robotech_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, String> {

    Optional<Club> findByUsuario_IdUsuario(String idUsuario);

    List<Club> findByNombreContainingIgnoreCase(String nombre);

    Optional<Club> findByUsuario(Usuario usuario);

    Optional<Club> findByCodigoClub(String codigoClub);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCorreoContacto(String correoContacto);

    boolean existsByTelefonoContacto(String telefonoContacto);

    // ✅ ESTE ERA EL QUE FALTABA
    @Query("""
        SELECT c
        FROM Club c
        JOIN FETCH c.usuario
    """)
    List<Club> findAllWithUsuario();
}
