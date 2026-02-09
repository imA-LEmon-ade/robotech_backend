package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);

    List<Usuario> findByEstado(EstadoUsuario estado);
    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByPasswordResetToken(String token);

    // En UsuarioRepository.java

    @Query("SELECT COUNT(u) FROM Usuario u JOIN Competidor c ON c.usuario.idUsuario = u.idUsuario WHERE c.clubActual.idClub = :idClub")
    long contarUsuariosPorClub(@Param("idClub") String idClub);


    boolean existsByDni(String dni);

    boolean existsByRolesContaining(RolUsuario rol);

    @Query("""
            SELECT u FROM Usuario u
            WHERE
                LOWER(COALESCE(u.dni, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(u.nombres, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(u.apellidos, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Usuario> buscar(@Param("q") String q, Pageable pageable);
}

